package ru.practicum.shareit.item.service;

/*
    Реализация сервиса для работы с вещами.
    Сервис получает из контроллера ItemController объекты ItemDto
    и возвращает объекты Item
 */

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemToDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public Item addItem(ItemDto itemDto, int userId) {
        if (!userStorage.getUsers().containsKey(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        return itemStorage.addItem(ItemToDto.toItem(itemDto), userId);
    }

    @Override
    public Item getItem(int id) {
        return itemStorage.getItemById(id);
    }

    @Override
    public List<Item> getAllItems(int userId) {
        if (!userStorage.getUsers().containsKey(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        Map<Integer, Item> temp = itemStorage.getAllItems();
        Map<Integer, Item> result = new LinkedHashMap<>();
        for (Item item : temp.values()) {
            if (item.getOwner() == userId)
                result.put(item.getId(), item);
        }
        return new ArrayList<>(result.values());
    }

    @Override
    public Item updateItem(ItemDto item, int itemId, int userId) {
        if (!userStorage.getUsers().containsKey(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        if (itemStorage.getItemById(itemId).getOwner() != userId)
            throw new ItemAccessRestrictException("Только владелец вещи может ее изменить");

        return itemStorage.updateItem(ItemToDto.toItem(item), itemId);
    }

    @Override
    public void deleteItem(int id, int userId) {
        if (itemStorage.getItemById(id).getOwner() != userId)
            throw new ItemAccessRestrictException("Только владелец вещи может ее удалить");
        itemStorage.deleteItem(id);
    }

    @Override
    public List<Item> searchItems(String request) {
        Map<Integer, Item> temp = itemStorage.getAllItems();
        Map<Integer, Item> result = new LinkedHashMap<>();
        if (!"".equals(request)) {
            for (Item item : temp.values()) {
                if (item.getAvailable()
                        && (item.getName().toLowerCase().contains(request.toLowerCase())
                        || item.getDescription().toLowerCase().contains(request.toLowerCase())))
                    result.put(item.getId(), item);
            }
        }
        return new ArrayList<>(result.values());
    }
}
