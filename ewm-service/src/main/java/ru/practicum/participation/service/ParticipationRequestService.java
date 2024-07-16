package ru.practicum.participation.service;

import ru.practicum.participation.dto.EventRequestStatusUpdate;
import ru.practicum.participation.dto.EventRequestStatusUpdateResponse;
import ru.practicum.participation.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequestToParticipateInEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResponse updateRequestStatusParticipateOwnerEvent(
            Long userId, Long eventId, EventRequestStatusUpdate eventRequestStatus);

    ParticipationRequestDto cancelRequestToParticipateInEvent(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsForOwnerEvent(Long userId, Long eventId);

    List<ParticipationRequestDto> getInfoOnRequestsForUserInOtherEvents(Long userId);
}
