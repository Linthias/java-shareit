package ru.practicum.shareit.user.storage;

/*
    Реализация хранилища пользователей.
    Проверки сделаны с помощью традиционных if,
    а не с помощью аннотаций, так как
    требуются разные коды ответов
 */

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exceptions.UserDataConflictException;
import ru.practicum.shareit.user.exceptions.UserIncompleteDataException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Component
public class UserStorage {
    private final Map<Integer, User> users;
    private static int count = 0;

    public UserStorage() {
        users = new LinkedHashMap<>();
    }

    public User addUser(User user) {
        if (user.getName() == null)
            throw new UserIncompleteDataException("Отсутствует имя пользователя");
        if ("".equals(user.getName()))
            throw new UserIncompleteDataException("Отсутствует имя пользователя");
        if (user.getEmail() == null)
            throw new UserIncompleteDataException("Отсутствует эл. почта пользователя");
        for (User temp : users.values()) {
            if (temp.getEmail().equals(user.getEmail()))
                throw new UserDataConflictException("Пользователь с таким адресом почты уже существует");
        }

        ++count;
        user.setId(count);
        users.put(user.getId(), user);
        return user;
    }

    public User getUserById(int id) {
        if (!users.containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");
        return users.get(id);
    }

    public Map<Integer, User> getAllUsers() {
        return users;
    }

    public User updateUser(User user, int userId) {
        if (!users.containsKey(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        User temp = users.get(userId);
        if (user.getName() != null && !"".equals(user.getName()))
            temp.setName(user.getName());
        if (user.getEmail() != null) {
            for (User temp2 : users.values()) {
                if (temp2.getEmail().equals(user.getEmail()))
                    throw new UserDataConflictException("Пользователь с таким адресом почты уже существует");
            }
            temp.setEmail(user.getEmail());
        }

        users.replace(temp.getId(), temp);
        return users.get(userId);
    }

    public void deleteUser(int id) {
        if (!users.containsKey(id))
            throw new UserNotFoundException("Пользователь " + id + " не найден");

        users.remove(id);
    }
}
