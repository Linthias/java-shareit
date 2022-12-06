package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.exceptions.ItemBadPageParamsException;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto postItem(@RequestHeader("X-Sharer-User-Id") int userId,
                            @RequestBody ItemDto item) {
        log.info("POST /items вещь добавлена userId=" + userId);
        return itemService.addItem(item, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@RequestHeader("X-Sharer-User-Id") int userId,
                                  @PathVariable int itemId,
                                  @RequestBody CommentInputDto comment) {
        log.info("POST /items/" + itemId + "/comment userId=" + userId);
        comment.setCreated(LocalDateTime.now());
        return itemService.addComment(comment, itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader("X-Sharer-User-Id") int userId,
                             @PathVariable int itemId,
                             @RequestBody ItemDto item) {
        log.info("PATCH /items/" + itemId + " вещь обновлена userId=" + userId);
        return itemService.updateItem(item, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWBookingsDto getItem(@RequestHeader(value = "X-Sharer-User-Id") int userId,
                                    @PathVariable int itemId) {
        log.info("GET /items/" + itemId + " вещь получена userId=" + userId);
        return itemService.getItem(itemId, userId);
    }

    @GetMapping
    public List<ItemWBookingsDto> getUserItems(@RequestHeader("X-Sharer-User-Id") int userId,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size) {
        log.info("GET /items userId=" + userId + " from=" + from + " size=" + size);
        return itemService.getAllItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchForItems(@RequestHeader(value = "X-Sharer-User-Id") int userId,
                                        @RequestParam String text,
                                        @RequestParam(required = false) Integer from,
                                        @RequestParam(required = false) Integer size) {
        log.info("GET /items/search userId=" + userId + " text=" + text + " from=" + from + " size=" + size);
        return itemService.searchItems(text, userId, from, size);
    }

    @ExceptionHandler(ItemIncompleteDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncompleteData(ItemIncompleteDataException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ItemBadPageParamsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncompleteData(ItemBadPageParamsException e) {
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
