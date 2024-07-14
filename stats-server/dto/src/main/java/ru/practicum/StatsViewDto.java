package ru.practicum;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@AllArgsConstructor
public class StatsViewDto {
    private String app;
    private String uri;
    private Long hits;
}
