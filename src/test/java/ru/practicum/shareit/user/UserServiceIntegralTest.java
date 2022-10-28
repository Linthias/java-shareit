package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@Rollback(false)
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIntegralTest {

    private final EntityManager em;
    private final UserService service;

    UserDto userDto1 = UserDto.builder()
            .id(1)
            .name("name")
            .email("email@ya.ru")
            .build();
    User user1 = User.builder()
            .id(1)
            .name("name")
            .email("email@ya.ru")
            .build();

    UserDto userDto2 = UserDto.builder()
            .id(2)
            .name("another name")
            .email("email@com.ru")
            .build();
    User user2 = User.builder()
            .id(2)
            .name("another name")
            .email("email@com.ru")
            .build();

    @Test
    void addUserTest() throws Exception {
        service.addUser(userDto1);

        TypedQuery<User> query = em.createQuery("select u from User u where u.id =: id", User.class);
        User result = query.setParameter("id", userDto1.getId()).getSingleResult();

        assertEquals(userDto1.getId(), result.getId());
        assertEquals(userDto1.getName(), result.getName());
        assertEquals(userDto1.getEmail(), result.getEmail());
    }

    @Test
    void updateUserTest() throws Exception {
        service.updateUser(userDto2, userDto1.getId());

        TypedQuery<User> query = em.createQuery("select u from User u where u.id =: id", User.class);
        User result = query.setParameter("id", userDto1.getId()).getSingleResult();

        assertEquals(userDto1.getId(), result.getId());
        assertEquals(userDto2.getName(), result.getName());
        assertEquals(userDto2.getEmail(), result.getEmail());
    }
}
