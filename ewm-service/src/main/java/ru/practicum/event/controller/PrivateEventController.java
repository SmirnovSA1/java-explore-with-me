package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoUpdatedUser;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getOwnerEvents(@PathVariable @Min(1L) Long userId,
                                              @RequestParam(required = false, defaultValue = "0") Integer from,
                                              @RequestParam(required = false, defaultValue = "10") Integer size) {
        return eventService.getOwnerEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                     @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createOwnerEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventDto getOwnerOneEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                         @PathVariable @NotNull @Min(1L) Long eventId) {
        return eventService.getOwnerEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateOwnerEvent(@PathVariable @NotNull @Min(1L) Long userId,
                                         @PathVariable @NotNull @Min(1L) Long eventId,
                                         @Valid @RequestBody EventDtoUpdatedUser eventDtoUpdatedUser) {
        return eventService.updateOwnerEvent(userId, eventId, eventDtoUpdatedUser);
    }
}
