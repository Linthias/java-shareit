package ru.practicum.shareit.request.dto;

/*
Класс-маппер для ItemRequest:
ItemRequest -> ItemRequestDto
ItemRequest <- ItemRequestInputDto
 */

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public class ItemRequestDtoMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> items) {
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestInputDto inputDto, int userId) {
        return ItemRequest.builder()
                .description(inputDto.getDescription())
                .created(inputDto.getCreated())
                .author(userId)
                .build();
    }
}
