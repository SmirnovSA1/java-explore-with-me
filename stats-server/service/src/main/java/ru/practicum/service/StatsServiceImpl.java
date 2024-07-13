package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.HitEndpointDto;
import ru.practicum.StatsViewDto;
import ru.practicum.exception.EndTimeBeforeStartTimeException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.HitEndpoint;
import ru.practicum.model.StatsView;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Transactional
    @Override
    public HitEndpointDto createHit(HitEndpointDto endpointHitDto) {
        HitEndpoint endpointHitSave = statsRepository.save(statsMapper.toHitEndpoint(endpointHitDto));
        return statsMapper.toHitEndpointDto(endpointHitSave);
    }

    @Transactional(readOnly = true)
    @Override
    public List<StatsViewDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkTime(start, end);
        List<StatsView> viewStats;

        if (uris == null || uris.isEmpty()) {
            viewStats = Boolean.TRUE.equals(unique)
                    ? statsRepository.findAllByDateBetweenAndUniqueIp(start, end)
                    : statsRepository.findAllByDateBetweenStartAndEnd(start, end);
        } else {
            viewStats = Boolean.TRUE.equals(unique)
                    ? statsRepository.findAllByDateBetweenAndUriAndUniqueIp(start, end, uris)
                    : statsRepository.findAllByDateBetweenAndUri(start, end, uris);
        }
        return statsMapper.toStatsViewDtoList(viewStats);
    }

    private void checkTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.equals(start)) {
            throw new EndTimeBeforeStartTimeException("Дата окончания не может быть раньше даты начала");
        }
    }
}
