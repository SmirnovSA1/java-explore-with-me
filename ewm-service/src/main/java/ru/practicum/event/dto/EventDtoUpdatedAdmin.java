package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.event.model.AdminState;
import ru.practicum.location.dto.LocationDto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventDtoUpdatedAdmin {
    @Size(min = 20, max = 2000)
    private String annotation;
    @Min(1L)
    private Long category;
    @Size(min = 20, max = 7000)
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    @Min(0)
    private Integer participantLimit;
    private Boolean requestModeration;
    private AdminState stateAction;
    @Size(min = 3, max = 120)
    private String title;
}
