package ru.practicum.shareit.booking.dto;
/*
Dto объект для бронирований.
Использутеся только при возвращении данных
с сервера (отправки пользователю)
 */

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.MinItemDto;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private int id;
    private LocalDateTime start;
    private LocalDateTime end;
    private MinItemDto item;
    private BookerDto booker;
    private BookingStatus status;
}
