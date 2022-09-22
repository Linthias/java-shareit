package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, int userId);

    ItemDto getItem(int id);

    List<ItemDto> getAllItems(int userId);

    ItemDto updateItem(ItemDto item, int itemId, int userId);

    void deleteItem(int id, int userId);

    List<ItemDto> searchItems(String request);
}
