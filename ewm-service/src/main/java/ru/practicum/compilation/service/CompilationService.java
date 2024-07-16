package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdatedCompilationDto;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(UpdatedCompilationDto updatedCompilationDto, Long compId);

    void deleteCompilation(Long compId);

    CompilationDto getCompilationOfEvents(Long compId);

    List<CompilationDto> getCompilationsOfEvents(Boolean pinned, Integer from, Integer size);
}
