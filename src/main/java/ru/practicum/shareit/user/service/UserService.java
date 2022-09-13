package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User addUser(UserDto userDto);

    User getUser(int id);

    List<User> getAllUsers();

    User updateUser(UserDto userDto, int id);

    void deleteUser(int id);
}
