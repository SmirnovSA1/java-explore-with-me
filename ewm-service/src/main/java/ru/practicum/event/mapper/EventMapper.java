package ru.practicum.event.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);
    LocationMapper locationMapper = Mappers.getMapper(LocationMapper.class);

    @Mapping(target = "id",
            expression = "java(null)")
    @Mapping(target = "annotation",
            source = "newEventDto.annotation")
    @Mapping(target = "category",
            source = "category")
    @Mapping(target = "confirmedRequests",
            expression = "java(0)")
    @Mapping(target = "createdOn",
            expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "description",
            source = "newEventDto.description")
    @Mapping(target = "eventDate",
            source = "newEventDto.eventDate")
    @Mapping(target = "initiator",
            source = "initiator")
    @Mapping(target = "location",
            expression = "java(locationMapper.toLocation(newEventDto.getLocation()))")
    @Mapping(target = "paid",
            expression = "java(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)")
    @Mapping(target = "participantLimit",
            expression = "java(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)")
    @Mapping(target = "requestModeration",
            expression = "java(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)")
    @Mapping(target = "eventState",
            expression = "java(ru.practicum.event.model.EventState.PENDING)")
    @Mapping(target = "title",
            source = "newEventDto.title")
    @Mapping(target = "views",
            expression = "java(0L)")
    Event toEvent(NewEventDto newEventDto, Category category, User initiator);

    @Mapping(target = "id",
            source = "oldEvent.id")
    @Mapping(target = "annotation",
            expression = "java(request.getAnnotation() != null ? request.getAnnotation() : oldEvent.getAnnotation())")
    @Mapping(target = "category",
            expression = "java(request.getCategory() != null ? categoryNew : oldEvent.getCategory())")
    @Mapping(target = "confirmedRequests",
            source = "oldEvent.confirmedRequests")
    @Mapping(target = "createdOn",
            source = "oldEvent.createdOn")
    @Mapping(target = "description",
            expression = "java(request.getDescription() != null ? request.getDescription() : oldEvent.getDescription())")
    @Mapping(target = "eventDate",
            expression = "java(request.getEventDate() != null ? request.getEventDate() : oldEvent.getEventDate())")
    @Mapping(target = "initiator",
            source = "oldEvent.initiator")
    @Mapping(target = "location",
            expression = "java(request.getLocation() != null" +
                    " ? locationMapper.toLocation(request.getLocation()) : oldEvent.getLocation())")
    @Mapping(target = "paid",
            expression = "java(request.getPaid() != null ? request.getPaid() : oldEvent.getPaid())")
    @Mapping(target = "participantLimit",
            expression = "java(request.getParticipantLimit() != null\n" +
                    " ? request.getParticipantLimit() : oldEvent.getParticipantLimit())\n" +
                    " .publishedOn(oldEvent.getPublishedOn())")
    @Mapping(target = "publishedOn",
            source = "oldEvent.publishedOn")
    @Mapping(target = "requestModeration",
            expression = "java(request.getRequestModeration() != null\n" +
                    " ? request.getRequestModeration() : oldEvent.getRequestModeration())")
    @Mapping(target = "eventState",
            source = "oldEvent.eventState")
    @Mapping(target = "title",
            expression = "java(request.getTitle() != null ? request.getTitle() : oldEvent.getTitle())")
    @Mapping(target = "views",
            source = "oldEvent.views")
    Event toUpdatedOwnerEvent(Event oldEvent, EventDtoUpdatedUser request, Category categoryNew);

    @Mapping(target = "id",
            source = "oldEvent.id")
    @Mapping(target = "annotation",
            expression = "java(request.getAnnotation() != null ? request.getAnnotation() : oldEvent.getAnnotation())")
    @Mapping(target = "category",
            expression = "java(request.getCategory() != null ? categoryNew : oldEvent.getCategory())")
    @Mapping(target = "confirmedRequests",
            source = "oldEvent.confirmedRequests")
    @Mapping(target = "createdOn",
            source = "oldEvent.createdOn")
    @Mapping(target = "description",
            expression = "java(request.getDescription() != null ? request.getDescription() : oldEvent.getDescription())")
    @Mapping(target = "eventDate",
            expression = "java(request.getEventDate() != null ? request.getEventDate() : oldEvent.getEventDate())")
    @Mapping(target = "initiator",
            source = "oldEvent.initiator")
    @Mapping(target = "location",
            expression = "java(request.getLocation() != null" +
                    " ? locationMapper.toLocation(request.getLocation()) : oldEvent.getLocation())")
    @Mapping(target = "paid",
            expression = "java(request.getPaid() != null ? request.getPaid() : oldEvent.getPaid())")
    @Mapping(target = "participantLimit",
            expression = "java(request.getParticipantLimit() != null\n" +
                    " ? request.getParticipantLimit() : oldEvent.getParticipantLimit())\n" +
                    " .publishedOn(oldEvent.getPublishedOn())")
    @Mapping(target = "publishedOn",
            source = "oldEvent.publishedOn")
    @Mapping(target = "requestModeration",
            expression = "java(request.getRequestModeration() != null\n" +
                    " ? request.getRequestModeration() : oldEvent.getRequestModeration())")
    @Mapping(target = "eventState",
            source = "oldEvent.eventState")
    @Mapping(target = "title",
            expression = "java(request.getTitle() != null ? request.getTitle() : oldEvent.getTitle())")
    @Mapping(target = "views",
            source = "oldEvent.views")
    Event toUpdatedAdminEvent(Event oldEvent, EventDtoUpdatedAdmin request, Category categoryNew);

    @Mapping(target = "category",
            expression = "java(categoryMapper.toCategoryDto(event.getCategory()))")
    @Mapping(target = "eventDate",
            expression = "java(event.getEventDate().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)))")
    @Mapping(target = "initiator",
            expression = "java(userMapper.toUserShortDto(event.getInitiator()))")
    EventShortDto toEventShortDto(Event event);

    List<EventShortDto> toEventShortDtoList(List<Event> eventList);

    @Mapping(target = "category",
            expression = "java(categoryMapper.toCategoryDto(event.getCategory()))")
    @Mapping(target = "createdOn",
            expression = "java(event.getCreatedOn().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)))")
    @Mapping(target = "eventDate",
            expression = "java(event.getEventDate().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)))")
    @Mapping(target = "initiator",
            expression = "java(userMapper.toUserShortDto(event.getInitiator()))")
    @Mapping(target = "location",
            expression = "java(locationMapper.toLocationDto(event.getLocation()))")
    @Mapping(target = "publishedOn",
            expression = "java(event.getPublishedOn() != null ?\n" +
                    " event.getPublishedOn().format(java.time.format.DateTimeFormatter.ofPattern(ru.practicum.Constant.PATTERN_DATE)) : null)")
    @Mapping(target = "state", source = "event.eventState")
    EventDto toEventDto(Event event);

    @Mapping(target = "state", source = "event.eventState")
    List<EventDto> toEventDtoList(List<Event> event);
}
