package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    @Query(" select i from Item i "
            + "where ( upper(i.name) like upper(concat('%', ?1, '%')) "
            + " or upper(i.description) like upper(concat('%', ?1, '%')) )"
            + " and i.available = true "
            + "order by i.id asc ")
    List<Item> search(String text);

    Integer countByOwner(int ownerId);

    List<Item> findByOwner(int owner);

    List<Item> findByOwnerOrderById(int owner);

    List<Item> findByRequestIdOrderById(int requestId);
}
