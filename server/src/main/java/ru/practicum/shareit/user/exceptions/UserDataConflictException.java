package ru.practicum.shareit.user.exceptions;

public class UserDataConflictException extends RuntimeException {
    public UserDataConflictException(String message) {
        super(message);
    }
}
