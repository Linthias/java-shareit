package ru.practicum.shareit.booking.model;

import lombok.Getter;

@Getter
public enum BookingStatus {
    WAITING,
    APPROVED,
    REJECTED,
    CANCELED;

    @Override
    public String toString() {
        return this.name();
    }
}
