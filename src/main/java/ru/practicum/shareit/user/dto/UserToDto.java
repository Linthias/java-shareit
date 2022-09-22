package ru.practicum.shareit.user.dto;

/*
    Реализация класса для перевода объектов User в UserDto и обратно
 */

import ru.practicum.shareit.user.model.User;

public class UserToDto {
    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
