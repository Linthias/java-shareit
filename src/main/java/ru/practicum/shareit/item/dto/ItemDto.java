package ru.practicum.shareit.item.dto;

/*
    Упрощенное представление класса Item
    для сокращения объемов передач
    (пока выглядит лишним, так как тесты требуют,
    чтобы возвращался полный объект Item)
*/

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ItemDto {
    private int id;
    private String name;
    private String description;
    private Boolean available;
}
