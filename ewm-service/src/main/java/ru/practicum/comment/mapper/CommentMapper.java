package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.CommentDtoUpdated;
import ru.practicum.comment.dto.CommentExtendedDto;
import ru.practicum.comment.dto.NewCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "id",
            expression = "java(null)")
    @Mapping(target = "text",
            source = "newCommentDto.text")
    @Mapping(target = "author",
            source = "user")
    @Mapping(target = "parentComment",
            source = "comment")
    @Mapping(target = "event",
            source = "event")
    @Mapping(target = "created",
            expression = "java(java.time.LocalDateTime.now())")
    Comment toComment(NewCommentDto newCommentDto, Event event, User user, Comment comment);

    @Mapping(target = "userName",
            source = "comment.author.name")
    @Mapping(target = "created",
            expression = "java(comment.getCreated().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)))")
    @Mapping(target = "updated",
            expression = "java(comment.getUpdated() != null ? " +
                    "comment.getUpdated().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)) : null)")
    @Mapping(target = "reply",
            expression = "java(comment.getReply() != null ? toCommentDto(comment.getReply()) : null)")
    CommentDto toCommentDto(Comment comment);

    @Mapping(target = "author",
            expression = "java(userMapper.toUserDto(comment.getAuthor()))")
    @Mapping(target = "created",
            expression = "java(comment.getCreated().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)))")
    @Mapping(target = "updated",
            expression = "java(comment.getUpdated() != null ? " +
                    "comment.getUpdated().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)) : null)")
    @Mapping(target = "event",
            expression = "java(eventMapper.toEventDto(comment.getEvent()))")
    @Mapping(target = "parentComment",
            expression = "java(toCommentDto(comment.getParentComment()))")
    CommentExtendedDto toCommentExtendedDto(Comment comment);

    @Mapping(target = "id",
            source = "comment.id")
    @Mapping(target = "text",
            source = "commentDtoUpdated.text")
    @Mapping(target = "author",
            source = "comment.author")
    @Mapping(target = "parentComment",
            source = "comment.parentComment")
    @Mapping(target = "event",
            source = "comment.event")
    @Mapping(target = "created",
            source = "comment.created")
    @Mapping(target = "updated",
            expression = "java(java.time.LocalDateTime.now())")
    Comment toCommentUpdated(CommentDtoUpdated commentDtoUpdated, Comment comment);

    List<CommentDto> toCommentDtoList(List<Comment> comments);

    List<CommentExtendedDto> toCommentExtendedDtoList(List<Comment> comments);
}
