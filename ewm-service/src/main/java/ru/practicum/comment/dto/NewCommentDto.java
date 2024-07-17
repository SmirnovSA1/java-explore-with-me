package ru.practicum.comment.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Length(min = 1, max = 100)
    private String text;
    @Positive
    private Long parentComment;
}
