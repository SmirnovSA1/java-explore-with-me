package ru.practicum.event.service;

import ru.practicum.event.dto.*;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventDto createOwnerEvent(Long userId, NewEventDto newEventDto);

    EventDto updateOwnerEvent(Long userId, Long eventId, EventDtoUpdatedUser eventDtoUpdatedUser);

    EventDto updateEventByAdmin(EventDtoUpdatedAdmin eventDtoUpdatedAdmin, Long eventId);

    EventDto getOwnerEvent(Long userId, Long eventId);

    EventDto getEvent(Long id, HttpServletRequest request);

    List<EventShortDto> getOwnerEvents(Long userId, Integer from, Integer size);

    List<EventDto> getEventsForAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                     LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                     Integer from, Integer size);

    List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort,
                                  Integer from, Integer size, HttpServletRequest request);
}
