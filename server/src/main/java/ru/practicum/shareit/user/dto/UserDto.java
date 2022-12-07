package ru.practicum.shareit.user.dto;

/*
    Упрощенное представление класса User
    для сокращения объемов передач
*/

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private int id;
    private String name;
    private String email;
}
