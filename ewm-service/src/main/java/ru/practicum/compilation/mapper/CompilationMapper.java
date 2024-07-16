package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdatedCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "events",
            source = "events")
    @Mapping(target = "pinned",
            expression = "java(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false)")
    @Mapping(target = "title",
            source = "newCompilationDto.title")
    Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events);

    @Mapping(target = "events",
            expression = "java(compilation.getEvents() != null ? eventMapper.toEventShortDtoList(compilation.getEvents()) : new ArrayList<>())")
    CompilationDto toCompilationDto(Compilation compilation);

    List<CompilationDto> toCompilationDtoList(List<Compilation> compilationList);

    @Mapping(target = "id",
            source = "oldCompilation.id")
    @Mapping(target = "events",
            expression = "java(updatedCompilationDto.getEvents() == null ? oldCompilation.getEvents() : events)")
    @Mapping(target = "pinned",
            expression = "java(updatedCompilationDto.getPinned() == null ? oldCompilation.getPinned() : updatedCompilationDto.getPinned())")
    @Mapping(target = "title",
            expression = "java(updatedCompilationDto.getTitle() == null ? oldCompilation.getTitle() : updatedCompilationDto.getTitle())")
    Compilation toUpdatedCompilation(Compilation oldCompilation, UpdatedCompilationDto updatedCompilationDto, List<Event> events);
}
