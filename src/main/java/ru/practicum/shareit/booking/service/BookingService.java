package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(BookingInputDto bookingDto, int userId);

    BookingDto approveBooking(int id, int userId, boolean isApproved);

    BookingDto getBooking(int id, int userId);

    List<BookingDto> getUserBookings(int userId, String state);

    List<BookingDto> getUserItemsBookings(int userId, String state);
}
