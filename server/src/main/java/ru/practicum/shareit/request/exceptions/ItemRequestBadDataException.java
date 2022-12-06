package ru.practicum.shareit.request.exceptions;

public class ItemRequestBadDataException extends RuntimeException {
    public ItemRequestBadDataException(String message) {
        super(message);
    }
}
