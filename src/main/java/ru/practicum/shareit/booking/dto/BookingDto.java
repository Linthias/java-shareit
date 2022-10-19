package ru.practicum.shareit.booking.dto;
/*
Dto объект для бронирований.
Использутеся только при возвращении данных
с сервера (отправки пользователю)
 */
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.MinItemDto;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class BookingDto {
    private int id;
    private LocalDateTime start;
    private LocalDateTime end;
    private MinItemDto item;
    private BookerDto booker;
    private BookingStatus status;

    public static BookingDto toBookingDto(Booking booking, String itemName, String bookerName) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(new MinItemDto(booking.getItem(), itemName))
                .booker(new BookerDto(booking.getBooker(), bookerName))
                .status(booking.getStatus())
                .build();
    }
}
