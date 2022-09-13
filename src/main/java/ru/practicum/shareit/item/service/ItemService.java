package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item addItem(ItemDto itemDto, int userId);

    Item getItem(int id);

    List<Item> getAllItems(int userId);

    Item updateItem(ItemDto item, int itemId, int userId);

    void deleteItem(int id, int userId);

    List<Item> searchItems(String request);
}
