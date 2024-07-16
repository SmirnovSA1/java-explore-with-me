package ru.practicum;

import lombok.*;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StatsViewDto {
    private String app;
    private String uri;
    private Long hits;
}
