package ru.practicum.shareit.item.dto;

/*
Наиболее полная версия dto для вещей.
Содержит CommentDto и MinBookingDto
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.MinBookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ItemWBookingsDto {
    private int id;
    private String name;
    private String description;
    private Boolean available;
    private MinBookingDto lastBooking;
    private MinBookingDto nextBooking;
    private List<CommentDto> comments;

    public static ItemWBookingsDto toItemWBookingsDto(Item item,
                                                      MinBookingDto lastBooking,
                                                      MinBookingDto nextBooking,
                                                      List<CommentDto> comments) {
        return ItemWBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }
}
