package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Map;

/**
 * TODO Sprint add-controllers.
 */
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public Item postItem(@RequestHeader("X-Sharer-User-Id") int userId, @RequestBody ItemDto item) {
        log.info("POST /items вещь добавлена");
        return itemService.addItem(item, userId);
    }

    @PatchMapping("/{itemId}")
    public Item patchItem(@RequestHeader("X-Sharer-User-Id") int userId, @PathVariable int itemId, @RequestBody ItemDto item) {
        log.info("PATCH /items/" + itemId + " вещь обновлена");
        return itemService.updateItem(item, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public Item getItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) int userId, @PathVariable int itemId) {
        log.info("GET /items/" + itemId + "вещь получена");
        return itemService.getItem(itemId);
    }

    @GetMapping
    public List<Item> getUserItems(@RequestHeader("X-Sharer-User-Id") int userId) {
        log.info("GET /items получен список для пользователя " + userId);
        return itemService.getAllItems(userId);
    }

    @GetMapping("/search")
    public List<Item> searchForItems(@RequestHeader(value = "X-Sharer-User-Id", required = false) int userId, @RequestParam String text) {
        log.info("GET /items/search получен список вещей");
        return itemService.searchItems(text);
    }

    @ExceptionHandler(ItemIncompleteDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncompleteData(ItemIncompleteDataException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleItemNotFound(ItemNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ItemAccessRestrictException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> handleAccessRestriction(ItemAccessRestrictException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
