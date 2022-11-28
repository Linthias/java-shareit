package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.exceptions.ItemRequestBadDataException;
import ru.practicum.shareit.request.exceptions.ItemRequestBadPageParams;
import ru.practicum.shareit.request.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestServiceUnitTest {
    ItemRequestRepository itemRequestRepository = mock(ItemRequestRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    ItemRepository itemRepository = mock(ItemRepository.class);
    ItemRequestService requestService =
            new ItemRequestServiceImpl(itemRequestRepository, userRepository, itemRepository);

    Item item = Item.builder()
            .id(3)
            .name("item name")
            .description("item description")
            .available(true)
            .owner(4)
            .requestId(1)
            .build();

    ItemRequestInputDto input = new ItemRequestInputDto("description",
            LocalDateTime.of(2007, 6, 29, 10, 15));

    ItemRequestDto output = ItemRequestDto.builder()
            .id(1)
            .description(input.getDescription())
            .created(input.getCreated())
            .items(List.of(ItemDtoMapper.toItemDto(item)))
            .build();

    ItemRequest semiResult = ItemRequest.builder()
            .id(1)
            .description(input.getDescription())
            .created(input.getCreated())
            .author(2)
            .build();

    ItemRequestDto result;

    @Test
    void addRequestTest() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository.save(any())).thenReturn(semiResult);

        result = requestService.addRequest(input, 2);

        assertEquals(output.getId(), result.getId());
        assertEquals(output.getDescription(), result.getDescription());
        assertEquals(output.getCreated(), result.getCreated());
    }

    @Test
    void addRequestTestFail() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);

        try {
            result = requestService.addRequest(new ItemRequestInputDto(null,
                    LocalDateTime.of(2007, 6, 29, 10, 15)), 2);
        } catch (Exception e) {
            assertEquals(ItemRequestBadDataException.class, e.getClass());
        }
    }

    @Test
    void getRequestTest() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository.findById(any())).thenReturn(Optional.of(semiResult));
        when(itemRepository.findByRequestIdOrderById(anyInt())).thenReturn(List.of(item));

        result = requestService.getRequest(output.getId(), semiResult.getId());

        assertEquals(output.getId(), result.getId());
        assertEquals(output.getDescription(), result.getDescription());
        assertEquals(output.getCreated(), result.getCreated());
    }

    @Test
    void getRequestTestFail() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository.findById(any())).thenReturn(Optional.empty());

        try {
            result = requestService.getRequest(output.getId(), semiResult.getId());
        } catch (Exception e) {
            assertEquals(ItemRequestNotFoundException.class, e.getClass());
        }
    }

    @Test
    void getAuthorRequestsTest() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository.findByAuthorOrderByCreatedDesc(semiResult.getAuthor()))
                .thenReturn(List.of(semiResult));
        when(itemRepository.findByRequestIdOrderById(semiResult.getId())).thenReturn(List.of(item));

        List<ItemRequestDto> resultList = requestService.getAuthorRequests(semiResult.getAuthor());

        assertEquals(1, resultList.size());
        assertEquals(output.getId(), resultList.get(0).getId());
        assertEquals(output.getDescription(), resultList.get(0).getDescription());
        assertEquals(output.getCreated(), resultList.get(0).getCreated());
    }

    @Test
    void getAuthorRequestsTestFail() throws Exception {
        when(userRepository.existsById(any())).thenReturn(false);

        try {
            List<ItemRequestDto> resultList = requestService.getAuthorRequests(semiResult.getAuthor());
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
        }
    }

    @Test
    void getOtherUsersRequestsTest() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "created"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findByRequestIdOrderById(anyInt())).thenReturn(List.of(item));

        List<ItemRequestDto> resultList
                = requestService.getOtherUsersRequests(semiResult.getAuthor() + 1, 0, 5);

        assertEquals(1, resultList.size());
        assertEquals(output.getId(), resultList.get(0).getId());
        assertEquals(output.getDescription(), resultList.get(0).getDescription());
        assertEquals(output.getCreated(), resultList.get(0).getCreated());
    }

    @Test
    void getOtherUsersRequestsNullPageParamsTest() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRequestRepository.findByAuthorNotInOrderByCreatedDesc(any())).thenReturn(List.of(semiResult));
        when(itemRepository.findByRequestIdOrderById(anyInt())).thenReturn(List.of(item));

        List<ItemRequestDto> resultList
                = requestService.getOtherUsersRequests(semiResult.getAuthor() + 1, null, null);

        assertEquals(1, resultList.size());
        assertEquals(output.getId(), resultList.get(0).getId());
        assertEquals(output.getDescription(), resultList.get(0).getDescription());
        assertEquals(output.getCreated(), resultList.get(0).getCreated());
    }

    @Test
    void getOtherUsersRequestsTestFail() throws Exception {
        when(userRepository.existsById(any())).thenReturn(true);

        try {
            List<ItemRequestDto> resultList
                    = requestService.getOtherUsersRequests(semiResult.getAuthor() + 1, null, 5);
        } catch (Exception e) {
            assertEquals(ItemRequestBadPageParams.class, e.getClass());
        }
    }
}
