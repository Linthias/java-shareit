package ru.practicum.shareit.booking.dto;

/*
Dto объект для получения данных от пользователя
 */

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingInputDto {
    private int itemId;
    private LocalDateTime start;
    private LocalDateTime end;
}
