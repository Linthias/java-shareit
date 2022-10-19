package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerOrderByStartDesc(int booker);

    List<Booking> findByItemInOrderByStartDesc(List<Integer> itemIds);

    List<Booking> findByItemInOrderByStartAsc(List<Integer> itemIds);

    List<Booking> findByBookerAndItemOrderByStartAsc(int booker, int item);
}
