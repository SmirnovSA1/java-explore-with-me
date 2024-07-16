package ru.practicum.model;

import lombok.*;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class StatsView {
    private String app;
    private String uri;
    private Long hits;
}
