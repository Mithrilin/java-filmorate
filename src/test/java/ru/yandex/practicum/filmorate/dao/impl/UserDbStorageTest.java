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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private static final LocalDate BIRTHDAY_USER_ONE = LocalDate.of(1986, 10, 25);
    private static final LocalDate BIRTHDAY_USER_TWO = LocalDate.of(1993, 11, 14);
    private static final LocalDate BIRTHDAY_USER_THREE = LocalDate.of(1973, 6, 3);
    private static final String NAME_USER_ONE = "Nick Name";
    private static final String NAME_USER_TWO = "Tom Soer";
    private static final String NAME_USER_THREE = "Kris Bem";
    private static final String LOGIN_USER_ONE = "dolore";
    private static final String LOGIN_USER_TWO = "soer";
    private static final String LOGIN_USER_THREE = "bem";
    private static final String EMAIL_USER_ONE = "dolore@mail.ru";
    private static final String EMAIL_USER_TWO = "soer@mail.ru";
    private static final String EMAIL_USER_THREE = "bem@mail.ru";
    private static User userOne = null;
    private static User userTwo = null;
    private static User userThree = null;
    private UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);

        userOne = new User(EMAIL_USER_ONE, LOGIN_USER_ONE, NAME_USER_ONE, BIRTHDAY_USER_ONE);
        userTwo = new User(EMAIL_USER_TWO, LOGIN_USER_TWO, NAME_USER_TWO, BIRTHDAY_USER_TWO);
        userThree = new User(EMAIL_USER_THREE, LOGIN_USER_THREE, NAME_USER_THREE, BIRTHDAY_USER_THREE);
    }


    @Test
    @DisplayName("Добавление пользователя")
    void testAddUserShouldBeEquals() {
        int userId = userDbStorage.addUser(userOne).getId();
        User savedUser = userDbStorage.getUserById(userId).get(0);

        assertNotNull(savedUser);
        assertEquals(userOne, savedUser);
    }

    @Test
    @DisplayName("Обновление пользователя")
    void testUpdateUserShouldBeEquals() {
        User savedUser = userDbStorage.addUser(userOne);
        userTwo.setId(savedUser.getId());

        userDbStorage.updateUser(userTwo);
        User updatedUser = userDbStorage.getUserById(savedUser.getId()).get(0);

        assertNotNull(updatedUser);
        assertEquals(userTwo, updatedUser);
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void testGetAllUsersShouldBeEquals() {
        List<User> users = new ArrayList<>();
        users.add(userOne);
        users.add(userTwo);
        int userOneId = userDbStorage.addUser(userOne).getId();
        userOne.setId(userOneId);
        int userTwoId = userDbStorage.addUser(userTwo).getId();
        userTwo.setId(userTwoId);

        List<User> returnedUsers = userDbStorage.getAllUsers();

        assertNotNull(returnedUsers);
        assertEquals(users, returnedUsers);
    }

    @Test
    @DisplayName("Получение всех пользователей при наличии друзей")
    void testGetAllUsersShouldBeEqualsWhenFriendsNotEmpty() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        int userTwoId = userDbStorage.addUser(userTwo).getId();
        userDbStorage.addFriend(userOneId, userTwoId);

        List<User> returnedUsers = userDbStorage.getAllUsers();

        assertNotNull(returnedUsers);
        assertEquals(2, returnedUsers.size());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUserShouldBeEquals() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        int userTwoId = userDbStorage.addUser(userTwo).getId();

        userDbStorage.deleteUser(userOneId);

        List<User> returnedUsers = userDbStorage.getAllUsers();
        assertEquals(1, returnedUsers.size());
        assertEquals(userTwoId, returnedUsers.get(0).getId());
    }

    @Test
    @DisplayName("Добавление друга")
    void testAddFriendShouldBe1() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        int userTwoId = userDbStorage.addUser(userTwo).getId();

        userDbStorage.addFriend(userOneId, userTwoId);

        User returnedUser = userDbStorage.getUserById(userOneId).get(0);
        assertEquals(1, returnedUser.getFriends().size());
    }

    @Test
    @DisplayName("Удаление друга")
    void testDeleteFriendShouldBe0() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        int userTwoId = userDbStorage.addUser(userTwo).getId();
        userDbStorage.addFriend(userOneId, userTwoId);

        userDbStorage.deleteFriend(userOneId, userTwoId);

        User returnedUser = userDbStorage.getUserById(userOneId).get(0);
        assertEquals(0, returnedUser.getFriends().size());
    }

    @Test
    @DisplayName("Получение списка друзей")
    void testGetAllFriendsShouldBe1() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        userOne.setId(userOneId);
        int userTwoId = userDbStorage.addUser(userTwo).getId();
        userTwo.setId(userTwoId);
        userDbStorage.addFriend(userOneId, userTwoId);

        List<User> friends = userDbStorage.getAllFriends(userOneId);

        assertNotNull(friends);
        assertEquals(1, friends.size());
    }

    @Test
    @DisplayName("Получение списка общих друзей")
    void testGetAllCommonFriendsShouldBeEquals() {
        int userOneId = userDbStorage.addUser(userOne).getId();
        int userTwoId = userDbStorage.addUser(userTwo).getId();
        int userThreeId = userDbStorage.addUser(userThree).getId();
        userDbStorage.addFriend(userOneId, userThreeId);
        userDbStorage.addFriend(userTwoId, userThreeId);

        List<User> commonFriends = userDbStorage.getAllCommonFriends(userOneId, userTwoId);

        assertNotNull(commonFriends);
        assertEquals(1, commonFriends.size());
        assertEquals(userThreeId, commonFriends.get(0).getId());
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser() {
        userDbStorage.addUser(userOne);
        userDbStorage.deleteUser(1);
        assertEquals(0, userDbStorage.getAllUsers().size());
    }
}