package ru.practicum.shareit.booking.dto;

/*
Класс-маппер для Booking:
Booking -> BookingDto
Booking <- BookingInputDto
 */

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.MinItemDto;

public class BookingDtoMapper {
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
