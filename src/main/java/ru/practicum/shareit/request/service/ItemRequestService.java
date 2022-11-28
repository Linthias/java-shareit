package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addRequest(ItemRequestInputDto inputDto, int userId);

    ItemRequestDto getRequest(int id, int userId);

    List<ItemRequestDto> getAuthorRequests(int userId);

    List<ItemRequestDto> getOtherUsersRequests(int userId, Integer from, Integer size);
}
