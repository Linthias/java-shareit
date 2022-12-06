package ru.practicum.shareit.booking.exceptions;

public class BookingAccessRestrictException extends RuntimeException {
    public BookingAccessRestrictException(String message) {
        super(message);
    }
}
