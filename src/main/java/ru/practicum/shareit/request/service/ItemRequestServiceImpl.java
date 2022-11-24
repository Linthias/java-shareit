package ru.practicum.shareit.request.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.exceptions.ItemRequestBadDataException;
import ru.practicum.shareit.request.exceptions.ItemRequestBadPageParams;
import ru.practicum.shareit.request.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@Component
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto addRequest(ItemRequestInputDto inputDto, int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        if (inputDto.getDescription() == null || inputDto.getDescription().equals(""))
            throw new ItemRequestBadDataException("Пустое описание в новом запросе от пользователя " + userId);

        return ItemRequestDtoMapper.toItemRequestDto(
                itemRequestRepository.save(ItemRequestDtoMapper.toItemRequest(inputDto, userId)), null);
    }

    @Override
    public ItemRequestDto getRequest(int id, int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        Optional<ItemRequest> tempRequest = itemRequestRepository.findById(id);
        if (tempRequest.isEmpty())
            throw new ItemRequestNotFoundException("Запрос " + id + " не найден");

        List<Item> tempItemList = itemRepository.findByRequestIdOrderById(id);
        List<ItemDto> result = new ArrayList<>();
        for (Item item : tempItemList) {
            result.add(ItemDtoMapper.toItemDto(item));
        }

        return ItemRequestDtoMapper.toItemRequestDto(tempRequest.get(), result);
    }

    @Override
    public List<ItemRequestDto> getAuthorRequests(int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        List<ItemRequest> tempRequestList = itemRequestRepository.findByAuthorOrderByCreatedDesc(userId);
        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest itemRequest : tempRequestList) {
            List<Item> tempItemList = itemRepository.findByRequestIdOrderById(itemRequest.getId());
            List<ItemDto> subResult = new ArrayList<>();
            for (Item item : tempItemList) {
                subResult.add(ItemDtoMapper.toItemDto(item));
            }

            result.add(ItemRequestDtoMapper.toItemRequestDto(itemRequest, subResult));
        }

        return result;
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(int userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        List<ItemRequest> tempRequestList;

        if (from == null && size == null)
            tempRequestList = itemRequestRepository.findByAuthorNotInOrderByCreatedDesc(Collections.singletonList(userId));
        else if (from == null || size == null || from < 0 || size <= 0)
            throw new ItemRequestBadPageParams("Параметры: from=" + from + " size=" + size);
        else {
            Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
            Pageable page = PageRequest.of(from, size, sortByCreated);
            Page<ItemRequest> requestPage = itemRequestRepository.findAll(page);

            tempRequestList = requestPage.get()
                    .filter(itemRequest -> itemRequest.getAuthor() != userId)
                    .collect(Collectors.toList());
        }

        List<ItemRequestDto> result = new ArrayList<>();
        for (ItemRequest itemRequest : tempRequestList) {
            List<Item> tempItemList = itemRepository.findByRequestIdOrderById(itemRequest.getId());
            List<ItemDto> subResult = new ArrayList<>();
            for (Item item : tempItemList) {
                subResult.add(ItemDtoMapper.toItemDto(item));
            }

            result.add(ItemRequestDtoMapper.toItemRequestDto(itemRequest, subResult));
        }

        return result;
    }
}
