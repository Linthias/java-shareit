package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.UserDataConflictException;
import ru.practicum.shareit.user.exceptions.UserIncompleteDataException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User postUser(@Valid @RequestBody UserDto userDto) {
        log.info("POST /users пользователь добавлен");
        return userService.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public User patchUser(@PathVariable int userId, @Valid @RequestBody UserDto userDto) {
        log.info("PATCH /users/" + userId + " пользователь обновлен");
        return userService.updateUser(userDto, userId);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable int userId) {
        log.info("GET /users/" + userId + " пользователь получен");
        return userService.getUser(userId);
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("GET /users получен список пользователей");
        return userService.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable int userId) {
        log.info("DELETE /users/" + userId + " пользователь удален");
        userService.deleteUser(userId);
    }

    @ExceptionHandler(UserDataConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDataConflict(UserDataConflictException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(UserIncompleteDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncompleteData(UserIncompleteDataException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
