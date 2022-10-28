package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerOrderByStartDesc(int booker);

    List<Booking> findByItemInOrderByStartDesc(List<Integer> itemIds);

    List<Booking> findByItemInOrderByStartAsc(List<Integer> itemIds);

    List<Booking> findByBookerAndItemOrderByStartAsc(int booker, int item);
}
