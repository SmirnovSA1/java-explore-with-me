package ru.practicum.location.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LocationDto {
    @NotNull
    private Float lat;
    @NotNull
    private Float lon;
}
