package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private static final LocalDate BIRTHDAY_USER_ONE = LocalDate.of(1986, 10, 25);
    private static final String NAME_USER_ONE = "Nick Name";
    private static final String LOGIN_USER_ONE = "dolore";
    private static final String EMAIL_USER_ONE = "dolore@mail.ru";
    private static User userOne = null;
    private UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);

        userOne = new User(EMAIL_USER_ONE, LOGIN_USER_ONE, NAME_USER_ONE, BIRTHDAY_USER_ONE);
    }


    @Test
    void updateUser() {
    }

    @Test
    void getUserById() {
    }

    @Test
    void getAllUsers() {
    }

    @Test
    void deleteUser() {
    }

    @Test
    void addFriend() {
    }

    @Test
    void deleteFriend() {
    }

    @Test
    void getAllFriends() {
    }

    @Test
    void getAllCommonFriends() {
    }

    @Test
    @DisplayName("Добавление пользователя")
    void testAddUserShouldBeEquals() {
        int userId = userDbStorage.addUser(userOne).getId();
        User savedUser = userDbStorage.getUserById(userId).get(0);

        assertNotNull(savedUser);
        assertEquals(userOne, savedUser);
    }
}