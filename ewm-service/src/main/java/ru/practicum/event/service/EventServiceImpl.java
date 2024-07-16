package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitEndpointDto;
import ru.practicum.StatsClient;
import ru.practicum.StatsViewDto;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.SearchEventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.Constant.NAME_SERVICE_APP;
import static ru.practicum.event.model.AdminState.PUBLISH_EVENT;
import static ru.practicum.event.model.AdminState.REJECT_EVENT;
import static ru.practicum.event.model.EventSort.VIEWS;
import static ru.practicum.event.model.EventState.*;
import static ru.practicum.event.model.UserState.CANCEL_REVIEW;
import static ru.practicum.event.model.UserState.SEND_TO_REVIEW;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final SearchEventRepository searchEventRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Transactional
    @Override
    public EventDto createOwnerEvent(Long userId, NewEventDto newEventDto) {
        getErrorIfTimeBeforeStartsIsLessThen(newEventDto.getEventDate(), 2);

        User initiator = getUser(userId);
        Category category = getCategory(newEventDto.getCategory());
        Location location = checkAndSaveLocation(newEventDto.getLocation());
        Event event = eventMapper.toEvent(newEventDto, category, initiator, location);
        Event createdEvent = eventRepository.save(event);

        return eventMapper.toEventDto(createdEvent);
    }

    @Transactional
    @Override
    public EventDto updateOwnerEvent(Long userId, Long eventId, EventDtoUpdatedUser request) {
        Event oldEvent = getExceptionIfThisNotOwnerOfEvent(eventId, userId);

        getExceptionIfStateEventPublished(oldEvent.getEventState());
        getErrorIfTimeBeforeStartsIsLessThen(request.getEventDate(), 2);
        getErrorIfTimeBeforeStartsIsLessThen(oldEvent.getEventDate(), 2);

        Location location = checkAndSaveLocation(request.getLocation());
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : oldEvent.getCategory();

        if (CANCEL_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toUpdatedOwnerEvent(oldEvent, request, category, location);
            oldEvent.setEventState(CANCELED);
            return eventMapper.toEventDto(eventRepository.save(oldEvent));
        } else if (SEND_TO_REVIEW.equals(request.getStateAction())) {
            oldEvent = eventMapper.toUpdatedOwnerEvent(oldEvent, request, category, location);
            oldEvent.setEventState(PENDING);
        }

        return eventMapper.toEventDto(eventRepository.save(oldEvent));
    }

    @Transactional
    @Override
    public EventDto updateEventByAdmin(EventDtoUpdatedAdmin request, Long eventId) {
        Event event = getEvent(eventId);
        Category category = request.getCategory() != null
                ? getCategory(request.getCategory()) : event.getCategory();
        Location location = checkAndSaveLocation(request.getLocation());
        request.setLocation(location);

        getErrorIfTimeBeforeStartsIsLessThen(request.getEventDate(), 1);
        getErrorIfTimeBeforeStartsIsLessThen(event.getEventDate(), 1);

        if (PUBLISH_EVENT.equals(request.getStateAction())) {
            if (event.getEventState().equals(PENDING)) {
                event = eventMapper.toUpdatedAdminEvent(event, request, category, location);
                event.setPublishedOn(LocalDateTime.now());
                event.setEventState(PUBLISHED);
            } else {
                getExceptionIfEventNotPending(eventId);
            }
        } else if (REJECT_EVENT.equals(request.getStateAction())) {
            if (!event.getEventState().equals(PUBLISHED)) {
                event = eventMapper.toUpdatedAdminEvent(event, request, category, location);
                event.setEventState(CANCELED);
            } else {
                getExceptionIfEventPublished(eventId);
            }
        }
        return eventMapper.toEventDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public EventDto getOwnerEvent(Long userId, Long eventId) {
        getUser(userId);
        Event event = getExceptionIfThisNotOwnerOfEvent(eventId, userId);
        return eventMapper.toEventDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public EventDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = getEvent(eventId);

        if (event.getEventState().equals(PUBLISHED)) {
            sendInfoAboutViewInStats(List.of(eventId), request);
            setViewsForOneEvents(event);
        } else {
            getExceptionIfEventNotPublished(eventId);
        }

        return eventMapper.toEventDto(event);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> eventList = eventRepository.findAllByInitiatorId(userId, pageable).orElse(new ArrayList<>());
        return eventMapper.toEventShortDtoList(eventList);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                Integer from, Integer size) {
        checkDateTime(rangeStart, rangeEnd);
        AdminSearchEventOption adminSearchEventOption = AdminSearchEventOption.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(from)
                .size(size)
                .build();

        List<Event> events = searchEventRepository.getEventsByCriteriaByAdmin(adminSearchEventOption);
        return eventMapper.toEventDtoList(events);
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort,
                                         Integer from, Integer size, HttpServletRequest request) {
        checkDateTime(rangeStart, rangeEnd);
        SearchEventOption searchEventOption = SearchEventOption.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();

        List<Event> events = searchEventRepository.getEventsByCriteriaByAll(searchEventOption);
        sendInfoAboutViewInStats(events.stream().map(Event::getId).collect(Collectors.toList()), request);
        events = setViewsForListEvents(events);

        if (VIEWS.equals(sort)) {
            events = events.stream().sorted(Comparator.comparing(Event::getViews)).collect(Collectors.toList());
        }

        return eventMapper.toEventShortDtoList(events);
    }

    private void getErrorIfTimeBeforeStartsIsLessThen(LocalDateTime verifiableTime, Integer plusHours) {
        if (verifiableTime != null && verifiableTime.isBefore(LocalDateTime.now().plusHours(plusHours))) {
            throw new ValidationException(
                    String.format("Field: eventDate. Error: должно содержать дату раньше текущей. " +
                            "Value: %s", verifiableTime),
                    Collections.singletonList(String.format("Дата и время, на которые запланировано мероприятие," +
                            " не могут быть ранее, чем через %d (два часа) после текущего момента", plusHours)));
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", userId),
                        Collections.singletonList("Идентификатора пользователя не существует")));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException(String.format("Категория с id %d не найдена", categoryId),
                        Collections.singletonList("Идентификатора категории не существует")));
    }

    private Location checkAndSaveLocation(Location newLocation) {
        if (newLocation == null) {
            return null;
        }

        Location location = locationRepository.findByLatAndLon(newLocation.getLat(), newLocation.getLon())
                .orElse(null);
        if (location == null) {
            return locationRepository.save(newLocation);
        }
        return location;
    }

    private Event getExceptionIfThisNotOwnerOfEvent(Long eventId, Long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", eventId),
                        Collections.singletonList("Идентификатора события не существует")));
    }

    private void getExceptionIfStateEventPublished(EventState eventState) {
        if (eventState.equals(PUBLISHED)) {
            throw new ConflictException("Событие не может быть опубликовано повторно",
                    Collections.singletonList("Можно изменить только отложенные на рассмотрение или отмененные события"));
        }
    }

    private void getExceptionIfEventNotPending(Long eventId) {
        throw new ConflictException("Событие не находится на расмотрении",
                Collections.singletonList("Событие может быть опубликовано только, если оно находится в статусе рассмотрения"));
    }

    private void getExceptionIfEventPublished(Long eventId) {
        throw new ConflictException("Невозможно отменить событие, т.к. оно уже опубликовано",
                Collections.singletonList("Событие должно находиться в статусе рассмотрения или отмененного"));
    }

    private void sendInfoAboutViewInStats(List<Long> eventsIds, HttpServletRequest request) {
        for (Long id : eventsIds) {
            statsClient.createHit(new HitEndpointDto(NAME_SERVICE_APP, "/events/" + id,
                    request.getRemoteAddr(), LocalDateTime.now()));
        }
    }

    private void setViewsForOneEvents(Event event) {
        List<String> uri = List.of("/events/" + event.getId());
        StatsViewDto statsViewDto = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), uri, true)
                .get(0);
        event.setViews(statsViewDto.getHits());
    }

    private void getExceptionIfEventNotPublished(Long eventId) {
        throw new NotFoundException("Событие не найдено", Collections.singletonList("Некорректный id"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", eventId),
                        Collections.singletonList("Идентификатора события не существует")));
    }

    private void checkDateTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (isNotNullTime(rangeStart, rangeEnd) && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата окончания не может быть раньше даты начала",
                    Collections.singletonList("Передана некорректная дата окончания"));
        }
    }

    private boolean isNotNullTime(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return rangeStart != null && rangeEnd != null;
    }

    private List<Event> setViewsForListEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return events;
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime minCreatedOn = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        List<StatsViewDto> statsViewDtoList = statsClient.getStats(minCreatedOn, LocalDateTime.now(), uris, true);
        Map<Long, Long> eventsViews = new HashMap<>();
        statsViewDtoList.forEach(v -> eventsViews.put(Long.valueOf(v.getUri().replace("/events/", "")),
                v.getHits() != null ? v.getHits() : 0));
        events = events.stream()
                .peek(event -> {
                    Long views = eventsViews.getOrDefault(event.getId(), 0L);
                    event.setViews(views);
                })
                .collect(Collectors.toList());
        return events;
    }
}
