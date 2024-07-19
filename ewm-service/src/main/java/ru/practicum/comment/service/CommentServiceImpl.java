package ru.practicum.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoUpdated;
import ru.practicum.comment.dto.CommentExtendedDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.event.model.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentRepository;

    @Transactional
    @Override
    public CommentDto createComment(Long eventId, Long userId, NewCommentDto newCommentDto) {
        Event event = getEvent(eventId);
        User user = getUser(userId);
        getExceptionIfReplyNotEventInitiator(event, userId, newCommentDto);
        Comment comment = getParentComment(eventId, newCommentDto);
        Comment createdComment = commentMapper.toComment(newCommentDto, event, user, comment);
        createdComment = commentRepository.save(createdComment);
        return commentMapper.toCommentDto(createdComment);
    }

    @Transactional
    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);
        commentRepository.delete(comment);
    }

    @Transactional
    @Override
    public void deleteCommentByAdmin(Long commentId) {
        getComment(commentId);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public CommentDto updateComment(Long commentId, Long userId, CommentDtoUpdated commentDtoUpdated) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);
        Comment updateComment = commentMapper.toCommentUpdated(commentDtoUpdated, comment);
        updateComment = commentRepository.save(updateComment);
        return commentMapper.toCommentDto(updateComment);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentDto getCommentUser(Long commentId, Long userId) {
        Comment comment = getCommentByIdAndAuthor(commentId, userId);
        return commentMapper.toCommentDto(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public CommentExtendedDto getCommentForAdmin(Long commentId) {
        Comment comment = getComment(commentId);
        return commentMapper.toCommentExtendedDto(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getAllCommentsUser(Long userId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> commentListUser = commentRepository.findAllByAuthorId(userId, pageable).orElse(new ArrayList<>());
        return commentMapper.toCommentDtoList(commentListUser);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getAllCommentsByEvent(Long eventId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> commentListEvent = commentRepository.findAllByEventIdAndParentComment(eventId, null, pageable).orElse(new ArrayList<>());
        List<Comment> commentListWithReply = setReplies(commentListEvent);
        return commentMapper.toCommentDtoList(commentListWithReply);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentExtendedDto> getAllCommentsByTextForAdmin(String text, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> commentListByText = commentRepository.findAllByText(text, pageable).orElse(new ArrayList<>());
        return commentMapper.toCommentExtendedDtoList(commentListByText);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentExtendedDto> getAllCommentsUserForAdmin(Long userId, Integer from, Integer size) {
        Pageable pageable = getPageable(from, size);
        List<Comment> commentListByUser = commentRepository.findAllByAuthorId(userId, pageable).orElse(new ArrayList<>());
        return commentMapper.toCommentExtendedDtoList(commentListByUser);
    }

    private Pageable getPageable(Integer from, Integer size) {
        return PageRequest.of(from / size, size);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", userId),
                        Collections.singletonList("Идентификатора пользователя не существует")));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findByIdAndEventState(eventId, PUBLISHED).orElseThrow(() ->
                new NotFoundException(String.format("Событие с id %d не найдено", eventId),
                        Collections.singletonList("Идентификатора события не существует")));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException(String.format("Комментарий с id %d не найден", commentId),
                        Collections.singletonList("Идентификатора комментария не существует")));
    }

    private Comment getParentComment(Long eventId, NewCommentDto newCommentDto) {
        return newCommentDto.getParentComment() == null ? null
                : commentRepository.findByIdAndEventId(newCommentDto.getParentComment(), eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Родительский комментарий по id события %d не найден", eventId),
                        Collections.singletonList("Родительский комментарий не найден")));
    }

    private Comment getCommentByIdAndAuthor(Long commentId, Long userId) {
        return commentRepository.findByIdAndAuthorId(commentId, userId).orElseThrow(() ->
                new NotFoundException(String.format("Комментарий с id %d не найден", commentId),
                        Collections.singletonList("Идентификатора комментария не существует")));
    }

    private void getExceptionIfReplyNotEventInitiator(Event event, Long userId, NewCommentDto newCommentDto) {
        if (newCommentDto.getParentComment() != null && !event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Отвечать на комментарии может только инициатор события",
                    Collections.singletonList("Пользователья не является инициатором события"));
        }
    }

    private Map<Long, Comment> getCommentsByIdInMap(List<Comment> replies) {
        Map<Long, Comment> repliesMap = new HashMap<>();
        replies.forEach(comment -> repliesMap.put(comment.getParentComment().getId(), comment));
        return repliesMap;
    }

    private List<Comment> setReplies(List<Comment> comments) {
        List<Long> commentsIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        List<Comment> replies = commentRepository.findAllByParentCommentIdIn(commentsIds).orElse(new ArrayList<>());
        Map<Long, Comment> repliesMap = getCommentsByIdInMap(replies);

        return comments.stream()
                .peek(comment -> {
                    if (repliesMap.containsKey(comment.getId())) {
                        Comment reply = repliesMap.get(comment.getId());
                        comment.setReply(reply);
                    }
                }).collect(Collectors.toList());
    }
}
