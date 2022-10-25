package ru.practicum.shareit.booking.exceptions;

public class BookingBadPageParamsException extends  RuntimeException {
    public BookingBadPageParamsException(String message) {
        super(message);
    }
}
