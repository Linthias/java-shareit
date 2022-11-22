package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import javax.persistence.TypedQuery;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemJpaTest {
    private final TestEntityManager em;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Test
    void searchTest() throws Exception {
        User owner1 = User.builder()
                .id(0)
                .name("owner1")
                .email("owner1@ya.ru")
                .build();

        User owner2 = User.builder()
                .id(0)
                .name("owner2")
                .email("owner2@ya.ru")
                .build();

        Item item1 = Item.builder()
                .id(0)
                .name("item")
                .description("item description")
                .available(true)
                .owner(1)
                .build();

        Item item2 = Item.builder()
                .id(0)
                .name("item")
                .description("item description")
                .available(false)
                .owner(2)
                .build();

        Item item3 = Item.builder()
                .id(0)
                .name("something")
                .description("something description")
                .available(true)
                .owner(1)
                .build();

        owner1.setId(userRepository.save(owner1).getId());
        owner2.setId(userRepository.save(owner2).getId());

        item1.setId(itemRepository.save(item1).getId());
        item2.setId(itemRepository.save(item2).getId());
        item3.setId(itemRepository.save(item3).getId());

        TypedQuery<Item> query
                = em.getEntityManager()
                .createQuery("select i from Item i where i.id =: id", Item.class);

        Item semiResult = query.setParameter("id", item1.getId()).getSingleResult();
        assertEquals(item1.getName(), semiResult.getName());
        assertEquals(item1.getDescription(), semiResult.getDescription());

        semiResult = query.setParameter("id", item2.getId()).getSingleResult();
        assertEquals(item2.getName(), semiResult.getName());
        assertEquals(item2.getDescription(), semiResult.getDescription());

        semiResult = query.setParameter("id", item3.getId()).getSingleResult();
        assertEquals(item3.getName(), semiResult.getName());
        assertEquals(item3.getDescription(), semiResult.getDescription());

        List<Item> result = itemRepository.search("item");
        assertEquals(1, result.size());

        result = itemRepository.search("description");
        assertEquals(2, result.size());

        result = itemRepository.search("something");
        assertEquals(1, result.size());
    }
}
