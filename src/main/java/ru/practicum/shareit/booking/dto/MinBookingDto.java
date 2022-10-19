package ru.practicum.shareit.booking.dto;

/*
Вспомогательный dto объект для
данных о бронировании в ItemDto
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinBookingDto {
    private int id;
    private int bookerId;
}
