package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserDto newUserDto);

    void deleteUser(Long userId);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);
}
