package ru.practicum.shareit.item.dto;

/*
Класс-маппер для Item:
Item -> ItemDto
Item <- ItemDto
Item -> ItemWBookingsDto
 */

import ru.practicum.shareit.booking.dto.MinBookingDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public class ItemDtoMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

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
