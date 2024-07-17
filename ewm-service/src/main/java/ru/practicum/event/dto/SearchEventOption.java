package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SearchEventOption {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private Integer from;
    private Integer size;
}
