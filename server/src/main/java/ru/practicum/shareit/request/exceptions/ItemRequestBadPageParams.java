package ru.practicum.shareit.request.exceptions;

public class ItemRequestBadPageParams extends RuntimeException {
    public ItemRequestBadPageParams(String message) {
        super(message);
    }
}
