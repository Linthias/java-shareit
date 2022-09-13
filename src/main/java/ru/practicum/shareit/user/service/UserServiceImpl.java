package ru.practicum.shareit.user.service;

/*
    Реализация сервиса для работы с пользователями.
    Сервис получает из контроллера UserController объекты UserDto
    и возвращает объекты User
 */

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserToDto;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User addUser(UserDto userDto) {
        return userStorage.addUser(UserToDto.toUser(userDto));
    }

    @Override
    public User getUser(int id) {
        return userStorage.getUserById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userStorage.getAllUsers().values());
    }

    @Override
    public User updateUser(UserDto userDto, int id) {
        if (!userStorage.getUsers().containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        return userStorage.updateUser(UserToDto.toUser(userDto), id);
    }

    @Override
    public void deleteUser(int id) {
        if (!userStorage.getUsers().containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        userStorage.deleteUser(id);
    }
}
