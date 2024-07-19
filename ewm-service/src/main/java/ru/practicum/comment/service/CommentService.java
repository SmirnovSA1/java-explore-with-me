package ru.practicum.comment.service;

import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoUpdated;
import ru.practicum.comment.dto.CommentExtendedDto;
import ru.practicum.comment.dto.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto createComment(Long eventId, Long userId, NewCommentDto newCommentDto);

    void deleteComment(Long commentId, Long userId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto updateComment(Long commentId, Long userId, CommentDtoUpdated commentDtoUpdated);

    CommentDto getCommentUser(Long commentId, Long userId);

    CommentExtendedDto getCommentForAdmin(Long commentId);

    List<CommentDto> getAllCommentsUser(Long userId, Integer from, Integer size);

    List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size);

    List<CommentExtendedDto> getAllCommentsByTextForAdmin(String text, Integer from, Integer size);

    List<CommentExtendedDto> getAllCommentsUserForAdmin(Long userId, Integer from, Integer size);
}
