package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, int userId);

    CommentDto addComment(CommentInputDto comment, int itemId, int userId);

    ItemWBookingsDto getItem(int id, int userId);

    List<ItemWBookingsDto> getAllItems(int userId);

    ItemDto updateItem(ItemDto item, int itemId, int userId);

    void deleteItem(int id, int userId);

    List<ItemDto> searchItems(String request);
}
