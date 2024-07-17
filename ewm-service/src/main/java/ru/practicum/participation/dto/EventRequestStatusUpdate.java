package ru.practicum.participation.dto;

import lombok.*;
import ru.practicum.participation.model.Status;

import javax.validation.constraints.NotNull;
import java.util.List;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdate {
    @NotNull
    private List<Long> requestIds;
    @NotNull
    private Status status;
}
