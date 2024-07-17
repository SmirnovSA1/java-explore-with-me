package ru.practicum.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserDto newUserDto) {
        return userService.createUser(newUserDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @NotNull @Min(1L) Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                 @RequestParam(defaultValue = "0", required = false) Integer from,
                                 @RequestParam(defaultValue = "10", required = false) Integer size) {
        return userService.getUsers(ids, from, size);
    }
}
