package ru.practicum.shareit.item.service;

/*
    Реализация сервиса для работы с вещами.
    Сервис получает из контроллера ItemController объекты ItemDto
    и возвращает объекты ItemDto
 */

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.MinBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoMapper;
import ru.practicum.shareit.item.dto.CommentInputDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.dto.ItemWBookingsDto;
import ru.practicum.shareit.item.exceptions.ItemAccessRestrictException;
import ru.practicum.shareit.item.exceptions.ItemIncompleteDataException;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Component
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserRepository userRepository,
                           BookingRepository bookingRepository,
                           CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        if (itemDto.getName() == null || itemDto.getName().equals(""))
            throw new ItemIncompleteDataException("Пустое имя");
        if (itemDto.getDescription() == null || itemDto.getDescription().equals(""))
            throw new ItemIncompleteDataException("Пустое описание");
        if (itemDto.getAvailable() == null)
            throw new ItemIncompleteDataException("Пустая доступность");

        Item temp = ItemDtoMapper.toItem(itemDto);
        temp.setOwner(userId);
        return ItemDtoMapper.toItemDto(itemRepository.save(temp));
    }

    @Override
    public CommentDto addComment(CommentInputDto comment, int itemId, int userId) {
        if (comment.getText() == null || "".equals(comment.getText()))
            throw new ItemIncompleteDataException("Пустой комментарий");
        Optional<User> tempUser = userRepository.findById(userId);
        if (tempUser.isEmpty())
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        if (!itemRepository.existsById(itemId))
            throw new ItemNotFoundException("Вещь " + itemId + " не найдена");

        // список бронирований пользователя определенной вещи,
        // отсортированный раньше -> позже по времени начала бронирования
        List<Booking> tempBookingList = bookingRepository.findByBookerAndItemOrderByStartAsc(userId, itemId);
        if (tempBookingList.isEmpty())
            throw new ItemIncompleteDataException("Пользователь " + userId + " не бронировал вещь " + itemId);

        // проверка работает даже в том случае, если у пользователя
        // к настоящему моменту уже есть законченные бронирования вещи
        int finishedBookings = 0;
        for (Booking booking : tempBookingList) {
            if (booking.getEnd().isBefore(LocalDateTime.now())) {
                ++finishedBookings;
            }
        }
        if (finishedBookings == 0)
            throw new ItemIncompleteDataException("Пользователь " + userId + "  еще не использовал вещь " + itemId);

        return CommentDtoMapper.toCommentDto(commentRepository.save(CommentDtoMapper.toComment(comment, itemId, userId)),
                tempUser.get().getName());
    }

    @Override
    public ItemWBookingsDto getItem(int id, int userId) {
        Optional<Item> tempItem = itemRepository.findById(id);
        if (tempItem.isEmpty())
            throw new ItemNotFoundException("Вещь " + id + " не найдена");

        if (userId == tempItem.get().getOwner())
            return findCloseBookings(tempItem.get());
        else {
            // вариант на случай, если запрос не от владельца
            return ItemDtoMapper.toItemWBookingsDto(tempItem.get(),
                    null,
                    null,
                    getCommentDtoList(tempItem.get()));
        }

    }

    @Override
    public List<ItemWBookingsDto> getAllItems(int userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        List<Item> temp;

        List<ItemWBookingsDto> result = new ArrayList<>();

        if (from == null && size == null)
            temp = itemRepository.findByOwnerOrderById(userId);
        else if (from == null || size == null || from < 0 || size <= 0)
            throw new RuntimeException();
        else {
            Sort sortById = Sort.by(Sort.Direction.ASC, "id");
            Pageable page = PageRequest.of(from, size, sortById);
            Page<Item> itemPage = itemRepository.findAll(page);

            temp = itemPage.get()
                    .filter(item -> item.getAvailable().equals(true))
                    .filter(item -> item.getOwner() == userId)
                    .collect(Collectors.toList());
        }

        for (Item item : temp) {
            result.add(findCloseBookings(item));
        }

        return result;
    }

    // метод, собирающий список комментариев к вещи
    private List<CommentDto> getCommentDtoList(Item item) {
        List<Comment> tempCommentList = commentRepository.findByItem(item.getId());
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : tempCommentList) {
            result.add(CommentDtoMapper.toCommentDto(comment, userRepository.findById(comment.getAuthor()).get().getName()));
        }

        return result;
    }

    // метод, определяющий два ближайших к текущей дате
    // бронирования (прошедшее/текущее и следующее)
    private ItemWBookingsDto findCloseBookings(Item item) {
        List<Booking> itemBookings = bookingRepository.findByItemInOrderByStartAsc(Collections.singletonList(item.getId()));

        ItemWBookingsDto result = null;

        switch (itemBookings.size()) {
            case 0:
                result = ItemDtoMapper.toItemWBookingsDto(item,
                        null,
                        null,
                        getCommentDtoList(item));
                break;
            case 1:
                Booking tempBooking = itemBookings.get(0);
                if (tempBooking.getStart().isBefore(LocalDateTime.now()))
                    result = ItemDtoMapper.toItemWBookingsDto(item,
                            new MinBookingDto(tempBooking.getId(), tempBooking.getBooker()),
                            null,
                            getCommentDtoList(item));
                else
                    result = ItemDtoMapper.toItemWBookingsDto(item,
                            null,
                            new MinBookingDto(tempBooking.getId(), tempBooking.getBooker()),
                            getCommentDtoList(item));
                break;
            default:
                Booking prevBooking = null;
                for (Booking booking : itemBookings) {
                    if (booking.getStart().isBefore(LocalDateTime.now())) {
                        prevBooking = booking;
                    } else {
                        if (prevBooking != null)
                            result = ItemDtoMapper.toItemWBookingsDto(item,
                                    new MinBookingDto(prevBooking.getId(), prevBooking.getBooker()),
                                    new MinBookingDto(booking.getId(), booking.getBooker()),
                                    getCommentDtoList(item));
                        else
                            result = ItemDtoMapper.toItemWBookingsDto(item,
                                    null,
                                    new MinBookingDto(booking.getId(), booking.getBooker()),
                                    getCommentDtoList(item));
                        break;
                    }
                }
        }

        return result;
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, int itemId, int userId) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");
        Optional<Item> temp = itemRepository.findById(itemId);
        if (temp.isEmpty())
            throw new ItemNotFoundException("Вещь " + itemId + " не найдена");


        if (temp.get().getOwner() != userId)
            throw new ItemAccessRestrictException("Только владелец вещи может ее изменить");

        if (itemDto.getName() != null)
            temp.get().setName(itemDto.getName());
        if (itemDto.getDescription() != null)
            temp.get().setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null)
            temp.get().setAvailable(itemDto.getAvailable());

        return ItemDtoMapper.toItemDto(itemRepository.save(temp.get()));
    }

    @Override
    public void deleteItem(int id, int userId) {
        Optional<Item> temp = itemRepository.findById(id);
        if (temp.isEmpty())
            throw new ItemNotFoundException("Вещь " + id + " не найдена");
        if (temp.get().getOwner() != userId)
            throw new ItemAccessRestrictException("Только владелец вещи может ее удалить");

        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> searchItems(String request, int userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException("Пользователь " + userId + " не найден");

        List<ItemDto> result = new ArrayList<>();

        if (!"".equals(request)) {
            List<Item> temp;

            if (from == null && size == null)
                temp = itemRepository.search(request);
            else if (from == null || size == null || from < 0 || size <= 0)
                throw new RuntimeException();
            else {
                Sort sortById = Sort.by(Sort.Direction.ASC, "id");
                Pageable page = PageRequest.of(from, size, sortById);
                Page<Item> itemPage = itemRepository.findAll(page);

                temp = itemPage.get()
                        .filter(item -> item.getAvailable().equals(true))
                        .filter(item -> item.getName().toLowerCase().contains(request.toLowerCase())
                                || item.getDescription().toLowerCase().contains(request.toLowerCase()))
                        .filter(item -> item.getOwner() == userId)
                        .collect(Collectors.toList());
            }

            for (Item item : temp) {
                result.add(ItemDtoMapper.toItemDto(item));
            }
        }

        return result;
    }
}
