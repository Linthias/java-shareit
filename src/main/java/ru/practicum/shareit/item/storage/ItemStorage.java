package ru.practicum.shareit.item.storage;

/*
    Реализация хранилища вещей.
    Проверки сделаны с помощью традиционных if,
    а не с помощью аннотаций, так как
    требуются разные коды ответов
 */

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Component
public class ItemStorage {
    private final Map<Integer, Item> items;
    private static int count;

    public ItemStorage() {
        items = new LinkedHashMap<>();
        count = 0;
    }

    public Item addItem(Item item, int ownerId) {
        if (item.getName() == null || "".equals(item.getName()))
            throw new ItemIncompleteDataException("Название вещи пусто");
        if (item.getDescription() == null || "".equals(item.getDescription()))
            throw new ItemIncompleteDataException("Описание вещи пусто");
        if (item.getAvailable() == null)
            throw new ItemIncompleteDataException("Статус вещи пуст");

        ++count;
        item.setId(count);
        item.setOwner(ownerId);
        items.put(item.getId(), item);
        return item;
    }

    public Item getItemById(int id) {
        if (!items.containsKey(id))
            throw new ItemNotFoundException("Вещь " + id + " не найдена");
        return items.get(id);
    }

    public Map<Integer, Item> getAllItems() {
        return items;
    }

    public Item updateItem(Item item, int itemId) {
        if (!items.containsKey(itemId))
            throw new ItemNotFoundException("Вещь " + itemId + " не найдена");

        Item temp = items.get(itemId);
        if (item.getName() != null && !"".equals(item.getName()))
            temp.setName(item.getName());
        if (item.getDescription() != null && !"".equals(item.getDescription()))
            temp.setDescription(item.getDescription());
        if (item.getAvailable() != null)
            temp.setAvailable(item.getAvailable());

        items.replace(temp.getId(), temp);
        return items.get(itemId);
    }

    public void deleteItem(int id) {
        if (!items.containsKey(id))
            throw new ItemNotFoundException("Вещь " + id + " не найдена");
        items.remove(id);
    }
}
