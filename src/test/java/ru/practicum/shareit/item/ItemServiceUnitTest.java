package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.MinBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.exceptions.ItemBadPageParamsException;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemServiceUnitTest {
    ItemRepository itemRepository = mock(ItemRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    BookingRepository bookingRepository = mock(BookingRepository.class);
    CommentRepository commentRepository = mock(CommentRepository.class);
    ItemService itemService
            = new ItemServiceImpl(itemRepository, userRepository, bookingRepository, commentRepository);

    ItemDto itemDto = ItemDto.builder()
            .id(1)
            .name("item name")
            .description("item description")
            .available(true)
            .requestId(2)
            .build();

    CommentInputDto commentInput
            = new CommentInputDto("comment text",
            LocalDateTime.of(2023, 6, 29, 10, 15));

    CommentDto commentDto = CommentDto.builder()
            .id(3)
            .text("comment text")
            .authorName("booker name")
            .created(LocalDateTime.of(2023, 6, 29, 10, 15))
            .build();

    Comment comment = Comment.builder()
            .id(3)
            .text("comment text")
            .item(1)
            .author(5)
            .created(LocalDateTime.of(2023, 6, 29, 10, 15))
            .build();

    Item item = Item.builder()
            .id(1)
            .name("item name")
            .description("item description")
            .available(true)
            .owner(4)
            .requestId(2)
            .build();

    User owner = User.builder()
            .id(4)
            .name("owner name")
            .email("owner@ya.ru")
            .build();

    User booker = User.builder()
            .id(5)
            .name("booker name")
            .email("booker@ya.ru")
            .build();

    Booking booking = Booking.builder()
            .id(6)
            .start(LocalDateTime.of(2007, 6, 29, 10, 0))
            .end(LocalDateTime.of(2007, 6, 29, 10, 15))
            .item(1)
            .booker(5)
            .status(BookingStatus.APPROVED)
            .build();

    ItemWBookingsDto output = ItemWBookingsDto.builder()
            .id(1)
            .name("item name")
            .description("item description")
            .available(true)
            .lastBooking(new MinBookingDto(booking.getId(), booker.getId()))
            .comments(List.of(commentDto))
            .build();

    CommentDto commentResult;
    ItemDto shortResult;
    ItemWBookingsDto fullResult;

    @Test
    void addItemTest() throws Exception {
        when(userRepository.existsById(anyInt())).thenReturn(true);
        when(itemRepository.save(any())).thenReturn(item);

        shortResult = itemService.addItem(itemDto, 4);

        assertEquals(itemDto.getId(), shortResult.getId());
        assertEquals(itemDto.getName(), shortResult.getName());
        assertEquals(itemDto.getDescription(), shortResult.getDescription());
        assertEquals(itemDto.getAvailable(), shortResult.getAvailable());
        assertEquals(itemDto.getRequestId(), shortResult.getRequestId());
    }

    @Test
    void addItemTestFail() throws Exception {
        when(userRepository.existsById(anyInt())).thenReturn(false);

        try {
            shortResult = itemService.addItem(itemDto, 84);
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
        }
    }

    @Test
    void addCommentTest() throws Exception {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(booker));
        when(itemRepository.existsById(item.getId())).thenReturn(true);
        when(bookingRepository
                .findByBookerAndItemOrderByStartAsc(booker.getId(), item.getId())).thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenReturn(comment);

        commentResult = itemService.addComment(commentInput, item.getId(), booker.getId());

        assertEquals(commentDto.getId(), commentResult.getId());
        assertEquals(commentDto.getText(), commentResult.getText());
        assertEquals(commentDto.getAuthorName(), commentResult.getAuthorName());
        assertEquals(commentDto.getCreated(), commentResult.getCreated());
    }

    @Test
    void addCommentTestFail() throws Exception {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(booker));
        when(itemRepository.existsById(item.getId())).thenReturn(true);
        when(bookingRepository
                .findByBookerAndItemOrderByStartAsc(booker.getId(), item.getId())).thenReturn(List.of());

        try {
            commentResult = itemService.addComment(commentInput, item.getId(), booker.getId());
        } catch (Exception e) {
            assertEquals(ItemIncompleteDataException.class, e.getClass());
        }
    }

    @Test
    void getItemTest() throws Exception {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository
                .findByItemInOrderByStartAsc(Collections.singletonList(item.getId())))
                .thenReturn(List.of(booking));
        when(commentRepository.findByItem(item.getId())).thenReturn(List.of(comment));
        when(userRepository.findById(comment.getAuthor())).thenReturn(Optional.of(booker));

        fullResult = itemService.getItem(item.getId(), owner.getId());

        assertEquals(output.getId(), fullResult.getId());
        assertEquals(output.getName(), fullResult.getName());
        assertEquals(output.getDescription(), fullResult.getDescription());
        assertEquals(output.getAvailable(), fullResult.getAvailable());
        assertEquals(output.getLastBooking().getId(), fullResult.getLastBooking().getId());
        assertEquals(output.getComments().size(), fullResult.getComments().size());
    }

    @Test
    void getItemTestFail() throws Exception {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        try {
            fullResult = itemService.getItem(item.getId(), owner.getId());
        } catch (Exception e) {
            assertEquals(ItemNotFoundException.class, e.getClass());
        }
    }

    @Test
    void getAllItemsTest() throws Exception {
        when(userRepository.existsById(anyInt())).thenReturn(true);
        when(itemRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(new PageImpl<>(List.of(item)));
        when(bookingRepository
                .findByItemInOrderByStartAsc(Collections.singletonList(item.getId())))
                .thenReturn(List.of(booking));
        when(commentRepository.findByItem(item.getId())).thenReturn(List.of(comment));
        when(userRepository.findById(comment.getAuthor())).thenReturn(Optional.of(booker));

        List<ItemWBookingsDto> resultList = itemService.getAllItems(owner.getId(), 0, 5);

        assertEquals(1, resultList.size());
        assertEquals(output.getId(), resultList.get(0).getId());
        assertEquals(output.getName(), resultList.get(0).getName());
        assertEquals(output.getDescription(), resultList.get(0).getDescription());
        assertEquals(output.getAvailable(), resultList.get(0).getAvailable());
        assertEquals(output.getLastBooking().getId(), resultList.get(0).getLastBooking().getId());
        assertEquals(output.getComments().size(), resultList.get(0).getComments().size());
    }

    @Test
    void getAllItemsNullParamsTest() throws Exception {
        when(userRepository.existsById(anyInt())).thenReturn(true);
        when(itemRepository.findByOwnerOrderById(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findByItemInOrderByStartAsc(Collections.singletonList(item.getId())))
                .thenReturn(List.of(booking));
        when(commentRepository.findByItem(item.getId())).thenReturn(List.of(comment));
        when(userRepository.findById(comment.getAuthor())).thenReturn(Optional.of(booker));

        List<ItemWBookingsDto> resultList = itemService.getAllItems(owner.getId(), null, null);

        assertEquals(1, resultList.size());
        assertEquals(output.getId(), resultList.get(0).getId());
        assertEquals(output.getName(), resultList.get(0).getName());
        assertEquals(output.getDescription(), resultList.get(0).getDescription());
        assertEquals(output.getAvailable(), resultList.get(0).getAvailable());
        assertEquals(output.getLastBooking().getId(), resultList.get(0).getLastBooking().getId());
        assertEquals(output.getComments().size(), resultList.get(0).getComments().size());
    }

    @Test
    void getAllItemsTestFail() throws Exception {
        when(userRepository.existsById(anyInt())).thenReturn(true);

        try {
            List<ItemWBookingsDto> resultList = itemService.getAllItems(owner.getId(), null, 5);
        } catch (Exception e) {
            assertEquals(ItemBadPageParamsException.class, e.getClass());
        }
    }

    @Test
    void updateItemTest() throws Exception {
        ItemDto localItemDto = ItemDto.builder()
                .id(1)
                .name("item update")
                .description("item update")
                .available(true)
                .requestId(2)
                .build();

        Item localItem = Item.builder()
                .id(1)
                .name("item update")
                .description("item update")
                .available(true)
                .owner(4)
                .requestId(2)
                .build();

        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(localItem);

        shortResult = itemService.updateItem(localItemDto, item.getId(), owner.getId());

        assertEquals(localItemDto.getId(), shortResult.getId());
        assertEquals(localItemDto.getName(), shortResult.getName());
        assertEquals(localItemDto.getDescription(), shortResult.getDescription());
        assertEquals(localItemDto.getAvailable(), shortResult.getAvailable());
    }

    @Test
    void updateItemTestFail() throws Exception {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        try {
            shortResult = itemService.updateItem(itemDto, item.getId(), booker.getId());
        } catch (Exception e) {
            assertEquals(ItemAccessRestrictException.class, e.getClass());
        }
    }

    @Test
    void deleteItemTest() throws Exception {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        itemService.deleteItem(item.getId(), owner.getId());

        verify(itemRepository, times(1)).deleteById(item.getId());
    }

    @Test
    void deleteItemTestFail() throws Exception {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        try {
            itemService.deleteItem(item.getId(), booker.getId());
        } catch (Exception e) {
            assertEquals(ItemAccessRestrictException.class, e.getClass());
        }
    }

    @Test
    void searchItemsTest() throws Exception {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(itemRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "id"))))
                .thenReturn(new PageImpl<>(List.of(item)));

        List<ItemDto> resultList = itemService.searchItems("item", owner.getId(), 0, 5);

        assertEquals(1, resultList.size());
        assertEquals(itemDto.getId(), resultList.get(0).getId());
        assertEquals(itemDto.getName(), resultList.get(0).getName());
        assertEquals(itemDto.getDescription(), resultList.get(0).getDescription());
        assertEquals(itemDto.getAvailable(), resultList.get(0).getAvailable());
    }

    @Test
    void searchItemsNullParamsTest() throws Exception {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(itemRepository.search("item")).thenReturn(List.of(item));

        List<ItemDto> resultList = itemService.searchItems("item", owner.getId(), null, null);

        assertEquals(1, resultList.size());
        assertEquals(itemDto.getId(), resultList.get(0).getId());
        assertEquals(itemDto.getName(), resultList.get(0).getName());
        assertEquals(itemDto.getDescription(), resultList.get(0).getDescription());
        assertEquals(itemDto.getAvailable(), resultList.get(0).getAvailable());
    }

    @Test
    void searchItemsTestFail() throws Exception {
        when(userRepository.existsById(owner.getId())).thenReturn(true);

        try {
            List<ItemDto> resultList = itemService.searchItems("item", owner.getId(), null, 5);
        } catch (Exception e) {
            assertEquals(ItemBadPageParamsException.class, e.getClass());
        }
    }
}
