package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIntegralTest {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final ItemService itemService;

    User owner = User.builder()
            .id(0)
            .name("owner")
            .email("owner@ya.ru")
            .build();

    User booker = User.builder()
            .id(0)
            .name("booker")
            .email("booker@ya.ru")
            .build();

    Item item = Item.builder()
            .id(0)
            .name("item")
            .description("item description")
            .available(true)
            .owner(owner.getId())
            .build();

    Booking booking = Booking.builder()
            .id(0)
            .start(LocalDateTime.of(2007, 6, 29, 10, 0))
            .end(LocalDateTime.of(2007, 6, 29, 10, 15))
            .item(item.getId())
            .booker(booker.getId())
            .status(BookingStatus.APPROVED)
            .build();


    CommentInputDto commentInput
            = new CommentInputDto("comment",
            LocalDateTime.of(2007, 6, 29, 10, 20));

    @Test
    void addCommentTest() throws Exception {
        owner.setId(userRepository.save(owner).getId());
        booker.setId(userRepository.save(booker).getId());

        item.setOwner(owner.getId());
        item.setId(itemRepository.save(item).getId());

        booking.setItem(item.getId());
        booking.setBooker(booker.getId());
        booking.setId(bookingRepository.save(booking).getId());

        CommentDto result = itemService.addComment(commentInput, item.getId(), booker.getId());

        assertEquals(commentInput.getText(), result.getText());
        assertEquals(booker.getName(), result.getAuthorName());
        assertEquals(commentInput.getCreated(), result.getCreated());
    }

    @Test
    void getAllItemsTest() throws Exception {
        owner.setId(userRepository.save(owner).getId());
        booker.setId(userRepository.save(booker).getId());

        item.setOwner(owner.getId());
        item.setId(itemRepository.save(item).getId());

        booking.setItem(item.getId());
        booking.setBooker(booker.getId());
        booking.setId(bookingRepository.save(booking).getId());

        itemService.addComment(commentInput, item.getId(), booker.getId());
        List<ItemWBookingsDto> result = itemService.getAllItems(owner.getId(), 0, 5);

        assertEquals(1, result.size());
        assertEquals(item.getName(), result.get(0).getName());
        assertEquals(item.getDescription(), result.get(0).getDescription());
        assertEquals(item.getAvailable(), result.get(0).getAvailable());
        assertEquals(booking.getBooker(), result.get(0).getLastBooking().getBookerId());
        assertEquals(null, result.get(0).getNextBooking());
        assertEquals(1, result.get(0).getComments().size());
        assertEquals(commentInput.getText(), result.get(0).getComments().get(0).getText());
    }
}
