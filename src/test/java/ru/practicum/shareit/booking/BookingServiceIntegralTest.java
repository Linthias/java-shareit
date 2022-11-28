package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
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
public class BookingServiceIntegralTest {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingService bookingService;

    User owner = User.builder()
            .id(1)
            .name("owner")
            .email("owner@ya.ru")
            .build();

    User booker = User.builder()
            .id(2)
            .name("booker")
            .email("booker@ya.ru")
            .build();

    Item item = Item.builder()
            .id(1)
            .name("item")
            .description("item description")
            .available(true)
            .owner(owner.getId())
            .build();

    BookingInputDto booking
            = new BookingInputDto(item.getId(),
            LocalDateTime.of(2023, 6, 29, 10, 0),
            LocalDateTime.of(2023, 6, 29, 10, 15));

    int bookingId = 0;

    @Test
    void getUserBookingsTest() {
        owner.setId(userRepository.save(owner).getId());
        booker.setId(userRepository.save(booker).getId());

        item.setOwner(owner.getId());
        item.setId(itemRepository.save(item).getId());

        booking.setItemId(item.getId());
        bookingId = bookingService.addBooking(booking, booker.getId()).getId();

        List<BookingDto> result = bookingService.getUserBookings(booker.getId(), "ALL", 0, 5);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
        assertEquals(item.getName(), result.get(0).getItem().getName());
        assertEquals(booker.getName(), result.get(0).getBooker().getName());
    }

    @Test
    void getUserItemsBookingsTest() {
        owner.setId(userRepository.save(owner).getId());
        booker.setId(userRepository.save(booker).getId());

        item.setOwner(owner.getId());
        item.setId(itemRepository.save(item).getId());

        booking.setItemId(item.getId());
        bookingId = bookingService.addBooking(booking, booker.getId()).getId();

        List<BookingDto> result = bookingService.getUserItemsBookings(owner.getId(), "ALL", 0, 5);

        assertEquals(1, result.size());
        assertEquals(bookingId, result.get(0).getId());
        assertEquals(item.getName(), result.get(0).getItem().getName());
        assertEquals(owner.getName(), result.get(0).getBooker().getName());
    }
}
