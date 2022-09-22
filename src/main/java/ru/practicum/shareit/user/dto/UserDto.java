package ru.practicum.shareit.user.dto;

/*
    Упрощенное представление класса User
    для сокращения объемов передач
    (пока выглядит лишним, так как тесты требуют,
    чтобы возвращался полный объект User)
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private int id;
    private String name;
    @Email
    private String email;
}
