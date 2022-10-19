package ru.practicum.shareit.user.service;

/*
    Реализация сервиса для работы с пользователями.
    Сервис получает из контроллера UserController объекты UserDto
    и возвращает объекты UserDto
 */

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.UserIncompleteDataException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().equals(""))
            throw new UserIncompleteDataException("Пустое имя");
        if (userDto.getEmail() == null || userDto.getEmail().equals(""))
            throw new UserIncompleteDataException("Пустая почта");

        return UserDto.toUserDto(userRepository.save(UserDto.toUser(userDto)));
    }

    @Override
    public UserDto getUser(int id) {
        Optional<User> temp = userRepository.findById(id);
        if (temp.isEmpty())
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        return UserDto.toUserDto(temp.get());
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> temp = userRepository.findAll();
        List<UserDto> result = new ArrayList<>();

        for (User user : temp) {
            result.add(UserDto.toUserDto(user));
        }

        return result;
    }

    @Override
    public UserDto updateUser(UserDto userDto, int id) {
        Optional<User> temp = userRepository.findById(id);
        if (temp.isEmpty())
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        userDto.setId(id);
        if (userDto.getName() == null)
            userDto.setName(temp.get().getName());
        if (userDto.getEmail() == null)
            userDto.setEmail(temp.get().getEmail());

        return UserDto.toUserDto(userRepository.save(UserDto.toUser(userDto)));
    }

    @Override
    public void deleteUser(int id) {
        if (!userRepository.existsById(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        userRepository.deleteById(id);
    }
}
