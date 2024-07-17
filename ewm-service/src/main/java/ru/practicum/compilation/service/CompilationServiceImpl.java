package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdatedCompilationDto;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        }

        Compilation created = compilationRepository.save(compilationMapper.toCompilation(newCompilationDto,
                events.isEmpty() ? null : events));
        return compilationMapper.toCompilationDto(created);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(UpdatedCompilationDto updatedCompilationDto, Long compId) {
        Compilation compilation = getCompilation(compId);
        List<Event> events = new ArrayList<>();

        if (updatedCompilationDto.getEvents() != null) {
            events = eventRepository.findAllById(updatedCompilationDto.getEvents());
        }

        compilation = compilationMapper.toUpdatedCompilation(compilation, updatedCompilationDto, events);
        Compilation updated = compilationRepository.save(compilation);
        return compilationMapper.toCompilationDto(updated);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = getCompilation(compId);
        compilationRepository.delete(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationOfEvents(Long compId) {
        Compilation compilation = getCompilation(compId);
        return compilationMapper.toCompilationDto(compilation);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CompilationDto> getCompilationsOfEvents(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        } else {
            compilations = compilationRepository.findAll(pageable).getContent();
        }

        return compilationMapper.toCompilationDtoList(compilations);
    }

    private Compilation getCompilation(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка не найдена", Collections.singletonList("Неверный id")));
    }
}
