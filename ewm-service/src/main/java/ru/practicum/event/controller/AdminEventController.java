package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.event.dto.EventDtoUpdatedAdmin;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.Constant.PATTERN_DATE;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventDto> getEventsForAdmin(@RequestParam(required = false) List<Long> users,
                                            @RequestParam(required = false) List<EventState> states,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                                LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = PATTERN_DATE)
                                                LocalDateTime rangeEnd,
                                            @RequestParam(defaultValue = "0", required = false) Integer from,
                                            @RequestParam(defaultValue = "10", required = false) Integer size) {
        return eventService.getEventsForAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventDto updateEventByAdmin(@Valid @RequestBody EventDtoUpdatedAdmin eventDtoUpdatedAdmin,
                                           @PathVariable @NotNull @Min(1L) Long eventId) {
        return eventService.updateEventByAdmin(eventDtoUpdatedAdmin, eventId);
    }
}
