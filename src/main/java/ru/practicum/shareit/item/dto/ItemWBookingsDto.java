package ru.practicum.shareit.item.dto;

/*
Наиболее полная версия dto для вещей.
Содержит CommentDto и MinBookingDto
 */

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.MinBookingDto;

import java.util.List;

@Data
@Builder
public class ItemWBookingsDto {
    private int id;
    private String name;
    private String description;
    private Boolean available;
    private MinBookingDto lastBooking;
    private MinBookingDto nextBooking;
    private List<CommentDto> comments;
}
