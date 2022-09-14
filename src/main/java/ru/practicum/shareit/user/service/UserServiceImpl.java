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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        return UserToDto.toUserDto(userStorage.addUser(UserToDto.toUser(userDto)));
    }

    @Override
    public UserDto getUser(int id) {
        return UserToDto.toUserDto(userStorage.getUserById(id));
    }

    @Override
    public List<UserDto> getAllUsers() {
        Map<Integer, User> temp = userStorage.getAllUsers();
        Map<Integer, UserDto> result = new LinkedHashMap<>();
        for (User user : temp.values()) {
            result.put(user.getId(), UserToDto.toUserDto(user));
        }
        return new ArrayList<>(result.values());
    }

    @Override
    public UserDto updateUser(UserDto userDto, int id) {
        if (!userStorage.getUsers().containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        return UserToDto.toUserDto(userStorage.updateUser(UserToDto.toUser(userDto), id));
    }

    @Override
    public void deleteUser(int id) {
        if (!userStorage.getUsers().containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        userStorage.deleteUser(id);
    }
}
