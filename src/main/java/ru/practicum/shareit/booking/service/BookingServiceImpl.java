package ru.practicum.shareit.booking.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.exceptions.BookingAccessRestrictException;
import ru.practicum.shareit.booking.exceptions.BookingBadPageParamsException;
import ru.practicum.shareit.booking.exceptions.BookingIncompleteDataException;
import ru.practicum.shareit.booking.exceptions.BookingNotFoundException;
import ru.practicum.shareit.booking.exceptions.BookingUnsupportedStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.exceptions.ItemRequestBadPageParams;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Component
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public BookingDto addBooking(BookingInputDto bookingDto, int userId) {
        if (bookingDto.getStart() == null)
            throw new BookingIncompleteDataException("Дата начала бронирования пуста");
        if (bookingDto.getEnd() == null)
            throw new BookingIncompleteDataException("Дата окончания бронирования пуста");

        if (bookingDto.getStart().isAfter(bookingDto.getEnd()))
            throw new BookingIncompleteDataException("Неверные даты бронирования");
        if (bookingDto.getStart().isBefore(LocalDateTime.now()))
            throw new BookingIncompleteDataException("Неверные даты бронирования");

        Optional<User> tempUser = userRepository.findById(userId);
        if (tempUser.isEmpty())
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        Optional<Item> tempItem = itemRepository.findById(bookingDto.getItemId());
        if (tempItem.isEmpty())
            throw new ItemNotFoundException("Вещь " + bookingDto.getItemId() + " не найдена");

        if (tempItem.get().getOwner() == userId)
            throw new UserNotFoundException("Владелец не может бронировать собственные вещи");
        if (!tempItem.get().getAvailable())
            throw new BookingAccessRestrictException("Вещь " + tempItem.get().getId() + " недоступна для бронирования");

        Booking result = BookingDtoMapper.toBooking(bookingDto, userId);
        return BookingDtoMapper.toBookingDto(bookingRepository.save(result),
                tempItem.get().getName(), tempUser.get().getName());
    }

    @Override
    public BookingDto approveBooking(int id, int userId, boolean isApproved) {
        Optional<Booking> result = bookingRepository.findById(id);
        if (result.isEmpty())
            throw new BookingNotFoundException("Бронирование " + id + " не найдено");
        Optional<Item> tempItem = itemRepository.findById(result.get().getItem());
        if (tempItem.isEmpty())
            throw new ItemNotFoundException("Вещь " + id + " не найдена");
        if (tempItem.get().getOwner() != userId)
            throw new UserNotFoundException("Только владелец вещи может одобрить бронирование");


        if (isApproved && result.get().getStatus().equals(BookingStatus.APPROVED))
            throw new BookingIncompleteDataException("Бронирование уже одобрено");
        else if (isApproved)
            result.get().setStatus(BookingStatus.APPROVED);
        else if (result.get().getStatus().equals(BookingStatus.REJECTED))
            throw new BookingIncompleteDataException("Бронирование уже отклонено");
        else
            result.get().setStatus(BookingStatus.REJECTED);

        // проверки на пользователя не нужны,
        // т.к. его наличие гарантируется существованием вещи (у каждой есть владелец)
        Optional<User> tempUser = userRepository.findById(userId);
        return BookingDtoMapper.toBookingDto(bookingRepository.save(result.get()),
                tempItem.get().getName(),
                tempUser.get().getName());
    }

    @Override
    public BookingDto getBooking(int id, int userId) {
        Optional<Booking> result = bookingRepository.findById(id);
        if (result.isEmpty())
            throw new BookingNotFoundException("Бронирование " + id + " не найдено");

        Optional<Item> tempItem = itemRepository.findById(result.get().getItem());
        if (tempItem.isEmpty())
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        if (result.get().getBooker() != userId && tempItem.get().getOwner() != userId)
            throw new UserNotFoundException("Получить информацию о бронировании могут лишь авторы и владельцы вещей");

        Optional<User> tempUser = userRepository.findById(userId);
        return BookingDtoMapper.toBookingDto(result.get(), tempItem.get().getName(), tempUser.get().getName());
    }

    @Override
    public List<BookingDto> getUserBookings(int userId, String state, Integer from, Integer size) {
        Optional<User> tempUser = userRepository.findById(userId);
        if (tempUser.isEmpty())
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        List<Booking> tempBookingList;

        if (from == null && size == null)
            tempBookingList = bookingRepository.findByBookerOrderByStartDesc(userId);
        else if (from == null || size == null || from < 0 || size <= 0)
            throw new BookingBadPageParamsException("Параметры: from=" + from + " size=" + size);
        else {
            Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
            Pageable page = PageRequest.of(from, size, sortByStart);
            Page<Booking> bookingPage = bookingRepository.findAll(page);

            tempBookingList = bookingPage.get()
                    .filter(booking -> booking.getBooker() == userId)
                    .collect(Collectors.toList());
        }

        // выборка по нужному состоянию.
        // т.к. в общем случае порядок бронирований (и их количество для одной вещи) заранее не известен,
        // то для заполнения данных для каждого dto объекта приходится запрашивать по одной вещи
        List<BookingDto> result = new ArrayList<>();
        switch (state) {
            case "ALL":
                for (Booking booking : tempBookingList) {
                    Item tempItem = itemRepository.findById(booking.getItem()).get();
                    result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                }
                break;
            case "CURRENT":
                for (Booking booking : tempBookingList) {
                    if (booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "PAST":
                for (Booking booking : tempBookingList) {
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "FUTURE":
                for (Booking booking : tempBookingList) {
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "WAITING":
                for (Booking booking : tempBookingList) {
                    if (booking.getStatus().equals(BookingStatus.WAITING)) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "REJECTED":
                for (Booking booking : tempBookingList) {
                    if (booking.getStatus().equals(BookingStatus.REJECTED)) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            default:
                throw new BookingUnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return result;
    }

    @Override
    public List<BookingDto> getUserItemsBookings(int userId, String state, Integer from, Integer size) {
        Optional<User> tempUser = userRepository.findById(userId);
        if (tempUser.isEmpty())
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        if (itemRepository.countByOwner(userId) == 0)
            throw new ItemNotFoundException("У пользователя " + userId + " нет вещей");


        List<Item> tempItemList = itemRepository.findByOwner(userId);

        // список идентификаторов вещей пользователя
        List<Integer> itemIds = new ArrayList<>();
        for (Item item : tempItemList) {
            itemIds.add(item.getId());
        }

        List<Booking> tempBookingList;

        if (from == null && size == null)
            tempBookingList = bookingRepository.findByItemInOrderByStartDesc(itemIds);
        else if (from == null || size == null || from < 0 || size <= 0)
            throw new BookingBadPageParamsException("Параметры: from=" + from + " size=" + size);
        else {
            Sort sortByStart = Sort.by(Sort.Direction.DESC, "start");
            Pageable page = PageRequest.of(from, size, sortByStart);
            Page<Booking> bookingPage = bookingRepository.findAll(page);

            HashSet<Integer> tempIds = new HashSet<>(itemIds);

            tempBookingList = bookingPage.get()
                    .filter(booking -> tempIds.contains(booking.getItem()))
                    .collect(Collectors.toList());
        }

        List<BookingDto> result = new ArrayList<>();
        switch (state) {
            case "ALL":
                for (Booking booking : tempBookingList) {
                    Item tempItem = itemRepository.findById(booking.getItem()).get();
                    result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                }
                break;
            case "CURRENT":
                for (Booking booking : tempBookingList) {
                    if (booking.getStart().isBefore(LocalDateTime.now())
                            && booking.getEnd().isAfter(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "PAST":
                for (Booking booking : tempBookingList) {
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "FUTURE":
                for (Booking booking : tempBookingList) {
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "WAITING":
                for (Booking booking : tempBookingList) {
                    if (booking.getStatus().equals(BookingStatus.WAITING)) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            case "REJECTED":
                for (Booking booking : tempBookingList) {
                    if (booking.getStatus().equals(BookingStatus.REJECTED)) {
                        Item tempItem = itemRepository.findById(booking.getItem()).get();
                        result.add(BookingDtoMapper.toBookingDto(booking, tempItem.getName(), tempUser.get().getName()));
                    }
                }
                break;
            default:
                throw new BookingUnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }

        return result;
    }
}
