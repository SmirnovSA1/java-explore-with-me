package ru.practicum.service;

import ru.practicum.HitEndpointDto;
import ru.practicum.StatsViewDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    HitEndpointDto createHit(HitEndpointDto endpointHitDto);

    List<StatsViewDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
