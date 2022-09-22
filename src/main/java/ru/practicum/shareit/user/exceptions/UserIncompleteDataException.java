package ru.practicum.shareit.user.exceptions;

public class UserIncompleteDataException extends RuntimeException {
    public UserIncompleteDataException(String message) {
        super(message);
    }
}
