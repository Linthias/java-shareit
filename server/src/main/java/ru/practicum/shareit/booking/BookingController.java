package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.exceptions.BookingAccessRestrictException;
import ru.practicum.shareit.booking.exceptions.BookingBadPageParamsException;
import ru.practicum.shareit.booking.exceptions.BookingIncompleteDataException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingUnsupportedStatusException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto postBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                  @RequestBody BookingInputDto bookingDto) {
        log.info("POST /bookings userId=" + userId);
        return bookingService.addBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                     @PathVariable int bookingId,
                                     @RequestParam Boolean approved) {
        log.info("PATCH /bookings/" + bookingId + " userId=" + userId + " bookingId=" + bookingId + " approved=" + approved);
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@RequestHeader("X-Sharer-User-Id") int userId,
                                 @PathVariable int bookingId) {
        log.info("GET /bookings/" + bookingId + " userId=" + userId + " bookingId=" + bookingId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                            @RequestParam(required = false) String state,
                                            @RequestParam(required = false) Integer from,
                                            @RequestParam(required = false) Integer size) {
        log.info("GET /bookings/ userId=" + userId + " state=" + state);
        return bookingService.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getUserItemsBookings(@RequestHeader("X-Sharer-User-Id") int userId,
                                                 @RequestParam(required = false) String state,
                                                 @RequestParam(required = false) Integer from,
                                                 @RequestParam(required = false) Integer size) {
        log.info("GET /bookings/owner userId=" + userId + " state=" + state);
        return bookingService.getUserItemsBookings(userId, state, from, size);
    }

    @ExceptionHandler(BookingIncompleteDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIncompleteData(BookingIncompleteDataException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BookingAccessRestrictException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAccessRestriction(BookingAccessRestrictException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BookingBadPageParamsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleAccessRestriction(BookingBadPageParamsException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookingNotFound(BookingNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(BookingUnsupportedStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnsupportedStatus(BookingUnsupportedStatusException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleItemNotFound(ItemNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(UserNotFoundException e) {
        return Map.of("error", e.getMessage());
    }
}
