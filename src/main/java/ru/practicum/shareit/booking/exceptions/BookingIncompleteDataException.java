package ru.practicum.shareit.booking.exceptions;

public class BookingIncompleteDataException extends RuntimeException {
    public BookingIncompleteDataException(String message) {
        super(message);
    }
}
