package ru.practicum.shareit.user;


import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.exceptions.UserIncompleteDataException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceUnitTest {
    UserRepository userRepository = mock(UserRepository.class);
    UserService userService = new UserServiceImpl(userRepository);

    UserDto userDto1 = UserDto.builder()
            .id(1)
            .name("name")
            .email("email@ya.ru")
            .build();
    User user1 = User.builder()
            .id(1)
            .name("name")
            .email("email@ya.ru")
            .build();

    UserDto userDto2 = UserDto.builder()
            .id(2)
            .name("another name")
            .email("email@com.ru")
            .build();
    User user2 = User.builder()
            .id(2)
            .name("another name")
            .email("email@com.ru")
            .build();

    @Test
    void addUserTest() throws Exception {
        when(userRepository.save(UserDtoMapper.toUser(userDto1))).thenReturn(user1);

        UserDto result = userService.addUser(userDto1);

        assertEquals(userDto1.getId(), result.getId());
        assertEquals(userDto1.getName(), result.getName());
        assertEquals(userDto1.getEmail(), result.getEmail());
    }

    @Test
    void addUserTestFail() throws Exception {
        UserDto localUserDto = UserDto.builder()
                .id(1)
                .name("")
                .email("email@ya.ru")
                .build();

        try {
            userService.addUser(localUserDto);
        } catch (Exception e) {
            assertEquals(UserIncompleteDataException.class, e.getClass());
            assertEquals("Пустое имя", e.getMessage());
        }

        localUserDto.setName(null);

        try {
            userService.addUser(localUserDto);
        } catch (Exception e) {
            assertEquals(UserIncompleteDataException.class, e.getClass());
            assertEquals("Пустое имя", e.getMessage());
        }

        localUserDto.setName("name");
        localUserDto.setEmail("");

        try {
            userService.addUser(localUserDto);
        } catch (Exception e) {
            assertEquals(UserIncompleteDataException.class, e.getClass());
            assertEquals("Пустая почта", e.getMessage());
        }

        localUserDto.setEmail(null);

        try {
            userService.addUser(localUserDto);
        } catch (Exception e) {
            assertEquals(UserIncompleteDataException.class, e.getClass());
            assertEquals("Пустая почта", e.getMessage());
        }
    }

    @Test
    void getUserTest() throws Exception {
        when(userRepository.findById(userDto1.getId())).thenReturn(Optional.of(user1));

        UserDto result = userService.getUser(userDto1.getId());

        assertEquals(userDto1.getId(), result.getId());
        assertEquals(userDto1.getName(), result.getName());
        assertEquals(userDto1.getEmail(), result.getEmail());
    }

    @Test
    void getUserTestFail() throws Exception {
        when(userRepository.findById(userDto1.getId() + 1)).thenReturn(Optional.empty());

        try {
            userService.getUser(userDto1.getId() + 1);
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
            assertEquals("Пользователь " + (userDto1.getId() + 1) + " не найден", e.getMessage());
        }
    }

    @Test
    void getAllUsersTest() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(userDto1.getName(), result.get(0).getName());
        assertEquals(userDto2.getName(), result.get(1).getName());
    }

    @Test
    void getAllUsersTestEmpty() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertEquals(0, result.size());
    }

    @Test
    void updateUserTest() throws Exception {
        UserDto localDto = UserDto.builder()
                .id(1)
                .name("update")
                .email("update@com.ru")
                .build();

        User localUser = User.builder()
                .id(1)
                .name("update")
                .email("update@com.ru")
                .build();

        when(userRepository.findById(userDto1.getId())).thenReturn(Optional.of(user1));
        when(userRepository.save(localUser)).thenReturn(localUser);



        UserDto result = userService.updateUser(localDto, userDto1.getId());
        assertEquals(localDto.getName(), result.getName());
        assertEquals(localDto.getEmail(), result.getEmail());
    }

    @Test
    void updateUserTestFail() throws Exception {
        when(userRepository.findById(userDto1.getId() + 100)).thenReturn(Optional.empty());

        try {
            userService.updateUser(userDto1, userDto1.getId() + 100);
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
            assertEquals("Пользователь " + (userDto1.getId() + 100) + " не найден", e.getMessage());
        }
    }

    @Test
    void deleteUserTest() throws Exception {
        when(userRepository.existsById(user1.getId())).thenReturn(true);

        userService.deleteUser(user1.getId());

        verify(userRepository, times(1)).deleteById(user1.getId());
    }

    @Test
    void deleteUserTestFail() throws Exception {
        when(userRepository.existsById(user1.getId())).thenReturn(false);

        try {
            userService.deleteUser(user1.getId());
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
            assertEquals("Пользователь " + user1.getId() + " не найден", e.getMessage());
        }
    }
}
