package ru.practicum.shareit.booking.exceptions;

public class BookingUnsupportedStatusException extends RuntimeException{
    public BookingUnsupportedStatusException(String message) {
        super(message);
    }
}
