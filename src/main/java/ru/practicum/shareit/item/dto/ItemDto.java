package ru.practicum.shareit.item.dto;

/*
Dto объект, использующийся при получении данных от пользователя
и обновлении существующих вещей
*/

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    private int id;
    private String name;
    private String description;
    private Boolean available;
}
