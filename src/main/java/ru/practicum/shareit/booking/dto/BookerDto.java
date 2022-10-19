package ru.practicum.shareit.booking.dto;

/*
Вспомогательный dto объект для
данных об авторе бронирования в BookingDto
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookerDto {
    private int id;
    private String name;
}
