package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.exceptions.BookingBadPageParamsException;
import ru.practicum.shareit.booking.exceptions.BookingIncompleteDataException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingUnsupportedStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.MinItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BookingServiceUnitTest {
    BookingRepository bookingRepository = mock(BookingRepository.class);
    UserRepository userRepository = mock(UserRepository.class);
    ItemRepository itemRepository = mock(ItemRepository.class);
    BookingService bookingService
            = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);

    BookingInputDto input = new BookingInputDto(1,
            LocalDateTime.of(2023, 6, 29, 10, 0),
            LocalDateTime.of(2023, 6, 29, 10, 15));

    BookingDto output = BookingDto.builder()
            .id(2)
            .start(LocalDateTime.of(2023, 6, 29, 10, 0))
            .end(LocalDateTime.of(2023, 6, 29, 10, 15))
            .item(new MinItemDto(1, "item name"))
            .booker(new BookerDto(3, "user name"))
            .status(BookingStatus.WAITING)
            .build();

    User user = User.builder()
            .id(3)
            .name("user name")
            .email("user@ya.ru")
            .build();

    User owner = User.builder()
            .id(4)
            .name("owner name")
            .email("owner@ya.ru")
            .build();

    Item item = Item.builder()
            .id(1)
            .name("item name")
            .description("item description")
            .available(true)
            .owner(4)
            .requestId(5)
            .build();

    Booking semiResult = Booking.builder()
            .id(2)
            .start(LocalDateTime.of(2023, 6, 29, 10, 0))
            .end(LocalDateTime.of(2023, 6, 29, 10, 15))
            .item(1)
            .booker(3)
            .status(BookingStatus.WAITING)
            .build();

    BookingDto result;
    List<BookingDto> listResult;

    @Test
    void addBookingTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(input.getItemId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(semiResult);

        result = bookingService.addBooking(input, user.getId());

        assertEquals(output.getId(), result.getId());
        assertEquals(output.getStart(), result.getStart());
        assertEquals(output.getEnd(), result.getEnd());
        assertEquals(output.getItem().getName(), result.getItem().getName());
        assertEquals(output.getBooker().getName(), result.getBooker().getName());
        assertEquals(output.getStatus(), result.getStatus());
    }

    @Test
    void addBookingTestFail() throws Exception {
        BookingInputDto localInput = new BookingInputDto(1,
                null,
                LocalDateTime.of(2023, 6, 29, 10, 15));

        try {
            result = bookingService.addBooking(localInput, user.getId());
        } catch (Exception e) {
            assertEquals(BookingIncompleteDataException.class, e.getClass());
        }
    }

    @Test
    void approveBookingTest() throws Exception {
        Booking localSemiResult = Booking.builder()
                .id(2)
                .start(LocalDateTime.of(2023, 6, 29, 10, 0))
                .end(LocalDateTime.of(2023, 6, 29, 10, 15))
                .item(1)
                .booker(3)
                .status(BookingStatus.APPROVED)
                .build();

        BookingDto localOutput = BookingDto.builder()
                .id(2)
                .start(LocalDateTime.of(2023, 6, 29, 10, 0))
                .end(LocalDateTime.of(2023, 6, 29, 10, 15))
                .item(new MinItemDto(1, "item name"))
                .booker(new BookerDto(3, "owner name"))
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(semiResult));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.save(any())).thenReturn(localSemiResult);

        result = bookingService.approveBooking(semiResult.getId(), item.getOwner(), true);

        assertEquals(localOutput.getId(), result.getId());
        assertEquals(localOutput.getStart(), result.getStart());
        assertEquals(localOutput.getEnd(), result.getEnd());
        assertEquals(localOutput.getItem().getName(), result.getItem().getName());
        assertEquals(localOutput.getBooker().getName(), result.getBooker().getName());
        assertEquals(localOutput.getStatus(), result.getStatus());
    }

    @Test
    void approveBookingTestFail() throws Exception {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.empty());

        try {
            result = bookingService.approveBooking(semiResult.getId() + 5, item.getOwner(), true);
        } catch (Exception e) {
            assertEquals(BookingNotFoundException.class, e.getClass());
        }
    }

    @Test
    void getBookingTest() throws Exception {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(semiResult));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        result = bookingService.getBooking(semiResult.getId(), user.getId());

        assertEquals(output.getId(), result.getId());
        assertEquals(output.getStart(), result.getStart());
        assertEquals(output.getEnd(), result.getEnd());
        assertEquals(output.getItem().getName(), result.getItem().getName());
        assertEquals(output.getBooker().getName(), result.getBooker().getName());
        assertEquals(output.getStatus(), result.getStatus());
    }

    @Test
    void getBookingTestFail() throws Exception {
        when(bookingRepository.findById(anyInt())).thenReturn(Optional.of(semiResult));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.empty());

        try {
            result = bookingService.getBooking(semiResult.getId(), user.getId());
        } catch (Exception e) {
            assertEquals(UserNotFoundException.class, e.getClass());
        }
    }

    @Test
    void getUserBookingsAllTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "ALL", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
        assertEquals(output.getBooker().getName(), listResult.get(0).getBooker().getName());
    }

    @Test
    void getUserBookingsAllNullParamsTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerOrderByStartDesc(user.getId())).thenReturn(List.of(semiResult));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "ALL", null, null);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
        assertEquals(output.getBooker().getName(), listResult.get(0).getBooker().getName());
    }

    @Test
    void getUserBookingsCurrentTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "CURRENT", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserBookingsPastTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "PAST", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserBookingsFutureTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "FUTURE", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
        assertEquals(output.getBooker().getName(), listResult.get(0).getBooker().getName());
    }

    @Test
    void getUserBookingsWaitingTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "WAITING", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
        assertEquals(output.getBooker().getName(), listResult.get(0).getBooker().getName());
    }

    @Test
    void getUserBookingsRejectedTest() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserBookings(user.getId(), "REJECTED", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserBookingsTestFail() throws Exception {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        try {
            listResult = bookingService.getUserBookings(user.getId(), "REJECTED", 0, null);
        } catch (Exception e) {
            assertEquals(BookingBadPageParamsException.class, e.getClass());
        }
    }

    @Test
    void getUserItemsBookingsAllTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "ALL", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
    }

    @Test
    void getUserItemsBookingsAllNullParamsTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository.findByItemInOrderByStartDesc(List.of(4))).thenReturn(List.of(semiResult));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "ALL", null, null);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserItemsBookingsCurrentTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "CURRENT", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserItemsBookingsPastTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "PAST", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserItemsBookingsFutureTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "FUTURE", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
    }

    @Test
    void getUserItemsBookingsWaitingTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "WAITING", 0, 5);

        assertEquals(1, listResult.size());
        assertEquals(output.getId(), listResult.get(0).getId());
        assertEquals(output.getItem().getName(), listResult.get(0).getItem().getName());
    }

    @Test
    void getUserItemsBookingsRejectedTest() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));
        when(itemRepository.findById(anyInt())).thenReturn(Optional.of(item));

        listResult = bookingService.getUserItemsBookings(owner.getId(), "REJECTED", 0, 5);

        assertEquals(0, listResult.size());
    }

    @Test
    void getUserItemsBookingsTestFail() throws Exception {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.countByOwner(owner.getId())).thenReturn(1);
        when(itemRepository.findByOwner(owner.getId())).thenReturn(List.of(item));
        when(bookingRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "start"))))
                .thenReturn(new PageImpl<>(List.of(semiResult)));

        try {
            listResult = bookingService.getUserItemsBookings(owner.getId(), "BAD STATE", 0, 5);
        } catch (Exception e) {
            assertEquals(BookingUnsupportedStatusException.class, e.getClass());
        }
    }
}
