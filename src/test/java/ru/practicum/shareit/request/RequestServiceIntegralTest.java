package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceIntegralTest {
    private final ItemRequestService requestService;
    private final UserService userService;
    private final ItemService itemService;

    UserDto itemOwner = UserDto.builder()
            .id(1)
            .name("owner")
            .email("owner@ya.ru")
            .build();

    UserDto requester = UserDto.builder()
            .id(2)
            .name("requester")
            .email("requester@ya.ru")
            .build();

    ItemRequestInputDto request
            = new ItemRequestInputDto("request description", LocalDateTime.now());

    ItemRequestInputDto itemOwnersRequest
            = new ItemRequestInputDto("owners request description", LocalDateTime.now());

    ItemDto item = ItemDto.builder()
            .id(1)
            .name("item")
            .description("item description")
            .available(true)
            .requestId(1)
            .build();

    int requestId1,
            requestId2;
    
    @Test
    void getAuthorRequestsTest() throws Exception {
        itemOwner.setId(userService.addUser(itemOwner).getId());
        requester.setId(userService.addUser(requester).getId());

        requestId1 = requestService.addRequest(request, requester.getId()).getId();
        item.setId(itemService.addItem(item, itemOwner.getId()).getId());

        List<ItemRequestDto> result = requestService.getAuthorRequests(requester.getId());

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(request.getDescription(), result.get(0).getDescription());
        assertEquals(request.getCreated(), result.get(0).getCreated());
        assertEquals(1, result.get(0).getItems().size());
        assertEquals(item.getName(), result.get(0).getItems().get(0).getName());
    }

    @Test
    void getOtherUsersRequestsTest() throws Exception {
        itemOwner.setId(userService.addUser(itemOwner).getId());
        requester.setId(userService.addUser(requester).getId());

        requestId1 = requestService.addRequest(request, requester.getId()).getId();
        item.setId(itemService.addItem(item, itemOwner.getId()).getId());
        requestId2 = requestService.addRequest(itemOwnersRequest, itemOwner.getId()).getId();

        List<ItemRequestDto> result = requestService.getOtherUsersRequests(requester.getId(), 0, 5);

        assertEquals(1, result.size());
        assertEquals(requestId2, result.get(0).getId());
        assertEquals(itemOwnersRequest.getDescription(), result.get(0).getDescription());
        assertEquals(itemOwnersRequest.getCreated(), result.get(0).getCreated());
        assertEquals(0, result.get(0).getItems().size());
    }
}
