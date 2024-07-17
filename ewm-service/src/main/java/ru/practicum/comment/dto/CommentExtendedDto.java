package ru.practicum.comment.dto;

import lombok.*;
import ru.practicum.event.dto.EventDto;
import ru.practicum.user.dto.UserDto;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentExtendedDto {
    private Long id;
    private String text;
    private UserDto author;
    private String created;
    private String updated;
    private EventDto event;
    private CommentDto parentComment;
}
