package ru.practicum.shareit.booking.dto;

/*
Dto объект для получения данных от пользователя
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingInputDto {
    private int itemId;
    private LocalDateTime start;
    private LocalDateTime end;

    public static Booking toBooking(BookingInputDto bookingDto, int bookerId) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(bookingDto.getItemId())
                .booker(bookerId)
                .status(BookingStatus.WAITING)
                .build();
    }
}
