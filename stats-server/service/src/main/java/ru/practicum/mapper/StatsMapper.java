package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.HitEndpointDto;
import ru.practicum.StatsViewDto;
import ru.practicum.model.HitEndpoint;
import ru.practicum.model.StatsView;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StatsMapper {
    HitEndpointDto toHitEndpointDto(HitEndpoint endpointHit);

    HitEndpoint toHitEndpoint(HitEndpointDto endpointHitDto);

    List<StatsViewDto> toStatsViewDtoList(List<StatsView> viewStats);

    StatsViewDto toStatsViewDto(StatsView viewStats);
}
