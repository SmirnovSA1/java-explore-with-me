package ru.practicum.participation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.participation.dto.EventRequestStatusUpdate;
import ru.practicum.participation.dto.EventRequestStatusUpdateResponse;
import ru.practicum.participation.dto.ParticipationRequestDto;
import ru.practicum.participation.mapper.ParticipationRequestMapper;
import ru.practicum.participation.model.ParticipationRequest;
import ru.practicum.participation.model.Status;
import ru.practicum.participation.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.practicum.event.model.EventState.PUBLISHED;
import static ru.practicum.participation.model.Status.*;

@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestMapper participationRequestMapper;

    @Transactional
    @Override
    public ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId) {
        validationEventId(eventId);
        User requester = getUser(userId);
        Event event = getEvent(eventId);

        getExceptionIfRepeatedRequest(userId, eventId);
        getExceptionIfEventNotPublished(event.getEventState());
        getExceptionIfExceededRequestLimit(event.getConfirmedRequests(), event.getParticipantLimit());
        getExceptionIfInitiatorEqualsRequester(event.getInitiator().getId(), requester.getId());

        ParticipationRequest participationRequest = getNewParticipateRequest(
                userId, eventId, event.getParticipantLimit(), event.getRequestModeration());

        if (participationRequest.getStatus().equals(CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        participationRequestRepository.save(participationRequest);
        return participationRequestMapper.toParticipationRequestDto(participationRequest);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResponse updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdate eventRequestStatus) {
        Event event = getEvent(eventId);
        getExceptionIfEventIsNotThisUser(event.getInitiator(), userId);
        getExceptionIfExceededRequestLimit(event.getConfirmedRequests(), event.getParticipantLimit());

        boolean isNotApplicationConfirmationRequired = (event.getParticipantLimit() == 0)
                || event.getRequestModeration().equals(false);

        if (isNotApplicationConfirmationRequired) {
            return new EventRequestStatusUpdateResponse(new ArrayList<>(), new ArrayList<>());
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllById(eventRequestStatus.getRequestIds());
        EventRequestStatusUpdateResponse updated = setRequestStatusAndSave(event, eventId, participationRequests,
                eventRequestStatus);

        return updated;
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId) {
        ParticipationRequest participationRequest = getParticipateRequest(requestId);
        getExceptionIfRequestIsNotThisUser(userId, participationRequest.getRequester());

        if (participationRequest.getStatus().equals(CONFIRMED)) {
            Event event = getEvent(participationRequest.getEvent());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        participationRequest.setStatus(CANCELED);
        ParticipationRequest cancelRequest = participationRequestRepository.save(participationRequest);

        return participationRequestMapper.toParticipationRequestDto(cancelRequest);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequestsForOwnerEvent(Long userId, Long eventId) {
        Event event = getEvent(eventId);
        getExceptionIfEventIsNotThisUser(event.getInitiator(), userId);
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByEvent(eventId).orElse(new ArrayList<>());

        return participationRequestMapper.toParticipationRequestDtoList(participationRequests);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId) {
        List<ParticipationRequest> participationRequests = participationRequestRepository
                .findAllByRequester(userId).orElse(new ArrayList<>());

        return participationRequestMapper.toParticipationRequestDtoList(participationRequests);
    }

    private EventRequestStatusUpdateResponse setRequestStatusAndSave(Event event, Long eventId,
                                                                   List<ParticipationRequest> participationRequests,
                                                                   EventRequestStatusUpdate eventRequestStatus) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        List<ParticipationRequest> updatedRequests = new ArrayList<>();

        for (ParticipationRequest request : participationRequests) {
            getExceptionIfStatusRequestNotPending(request.getStatus());
            boolean isPotentialParticipant = request.getEvent().equals(eventId)
                    && eventRequestStatus.getStatus().equals(CONFIRMED)
                    && (event.getParticipantLimit() == 0 || event.getConfirmedRequests() < event.getParticipantLimit());
            if (isPotentialParticipant) {
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                request.setStatus(CONFIRMED);
                updatedRequests.add(request);
                confirmedRequests.add(participationRequestMapper.toParticipationRequestDto(request));
                continue;
            }

            request.setStatus(REJECTED);
            updatedRequests.add(request);
            rejectedRequests.add(participationRequestMapper.toParticipationRequestDto(request));
        }
        eventRepository.save(event);
        participationRequestRepository.saveAll(updatedRequests);
        return new EventRequestStatusUpdateResponse(confirmedRequests, rejectedRequests);
    }

    private ParticipationRequest getNewParticipateRequest(Long userId, Long eventId, Integer participantLimit,
                                                          Boolean requestModeration) {
        return new ParticipationRequest(null, LocalDateTime.now(), eventId, userId,
                participantLimit == 0 ? CONFIRMED : requestModeration.equals(true) ? PENDING : CONFIRMED);
    }

    private void getExceptionIfEventIsNotThisUser(User initiator, Long userId) {
        if (!initiator.getId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором события",
                    Collections.singletonList("Некорректный id события или id пользователя"));
        }
    }

    private void getExceptionIfRequestIsNotThisUser(Long userId, Long requesterId) {
        if (!userId.equals(requesterId)) {
            throw new ConflictException("Запрос на участие принадлежит другому пользователю",
                    Collections.singletonList("Некорректный запрос на участие или id пользователя"));
        }
    }

    private void getExceptionIfExceededRequestLimit(Integer confirmedRequests, Integer participantLimit) {
        if (confirmedRequests.equals(participantLimit) && participantLimit != 0) {
            throw new ConflictException("Достигнут лимит количества запросов на участие",
                    Collections.singletonList("Превышен лимит участников"));
        }
    }

    private void getExceptionIfEventNotPublished(EventState eventState) {
        if (!eventState.equals(PUBLISHED)) {
            throw new ConflictException("Вы не можете принять участие в не опубликованном событии",
                    Collections.singletonList("Событие не опубликовано"));
        }
    }

    private void getExceptionIfRepeatedRequest(Long userId, Long eventId) {
        ParticipationRequest request = participationRequestRepository.findByRequesterAndEvent(userId, eventId).orElse(null);
        if (request != null) {
            throw new ConflictException("Нельзя добавить повторный запрос на участие",
                    Collections.singletonList("Повторный запрос на участие"));
        }
    }

    private void getExceptionIfStatusRequestNotPending(Status state) {
        if (!state.equals(PENDING)) {
            throw new ConflictException("Статус может быть изменен только для находящихся на рассмотрении",
                    Collections.singletonList("Статус не соответствует статусу на рассмотрении"));
        }
    }

    private void getExceptionIfInitiatorEqualsRequester(Long initiatorId, Long requesterId) {
        if (initiatorId.equals(requesterId)) {
            throw new ConflictException("Инициатор события не может добавить запрос на участие в своем событии",
                    Collections.singletonList("Инициатор является запрашивающим"));
        }
    }

    private void validationEventId(Long eventId) {
        if (eventId == null || eventId < 1) {
            throw new ValidationException("Некорректные данные",
                    List.of("Должен быть передан id события", "id события должен быть положительным"));
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", userId),
                        Collections.singletonList("Идентификатора пользователя не существует")));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", eventId),
                        Collections.singletonList("Идентификатора события не существует")));
    }

    private ParticipationRequest getParticipateRequest(Long requestId) {
        return participationRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException(String.format("Запрос на участие с id %d не найден", requestId),
                        Collections.singletonList("Идентификатора запрос на участие не существует")));
    }
}
