package ru.practicum.shareit.item.dto;

/*
    Реализация класса для перевода объектов Item в ItemDto и обратно
 */

import ru.practicum.shareit.item.model.Item;

public class ItemToDto {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }
}
