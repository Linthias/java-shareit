package ru.practicum.shareit.item.dto;

/*
Сокращенная версия dto для вещей.
Используется в BookingDto
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinItemDto {
    private int id;
    private String name;
}
