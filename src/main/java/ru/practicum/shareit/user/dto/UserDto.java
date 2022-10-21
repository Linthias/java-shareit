package ru.practicum.shareit.user.dto;

/*
    Упрощенное представление класса User
    для сокращения объемов передач
*/

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@Builder
public class UserDto {
    private int id;
    private String name;
    @Email
    private String email;
}
