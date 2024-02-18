package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
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
    private FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);

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
    @DisplayName("Получение списка рекомендованных фильмов")
    void testGetRecommendationsShouldBeEquals() {
        int user1Id = userDbStorage.addUser(userOne).getId();
        int user2Id = userDbStorage.addUser(userTwo).getId();
        int user3Id = userDbStorage.addUser(userThree).getId();
        int user4Id = userDbStorage.addUser(new User("444@mail.ru", "log4", "name4", BIRTHDAY_USER_ONE)).getId();
        int user5Id = userDbStorage.addUser(new User("555@mail.ru", "log5", "name5", BIRTHDAY_USER_ONE)).getId();
        int user6Id = userDbStorage.addUser(new User("666@mail.ru", "log6", "name6", BIRTHDAY_USER_ONE)).getId();

        int film1Id = filmDbStorage.addFilm(new Film("filmName 1", "description 1",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film2Id = filmDbStorage.addFilm(new Film("filmName 2", "description 2",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film3Id = filmDbStorage.addFilm(new Film("filmName 3", "description 3",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film4Id = filmDbStorage.addFilm(new Film("filmName 4", "description 4",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film5Id = filmDbStorage.addFilm(new Film("filmName 5", "description 5",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film6Id = filmDbStorage.addFilm(new Film("filmName 6", "description 6",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film9Id = filmDbStorage.addFilm(new Film("filmName 9", "description 9",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();
        int film10Id = filmDbStorage.addFilm(new Film("filmName 10", "description 10",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"))).getId();

        // фильм для рекомендации
        Film film7 = new Film("filmName 7", "description 7",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"));
        int film7Id = filmDbStorage.addFilm(film7).getId();
        film7.setId(film7Id);
        // фильм для рекомендации
        Film film8 = new Film("filmName 8", "description 8",
                LocalDate.of(1986, 10, 25), 100, new Mpa(1, "G"));
        int film8Id = filmDbStorage.addFilm(film8).getId();
        film8.setId(film8Id);

        // Целевой пользователь
        filmDbStorage.addMark(film1Id, user1Id, 10);
        filmDbStorage.addMark(film4Id, user1Id, 3);
        filmDbStorage.addMark(film6Id, user1Id, 8);

        // Похожий по оценкам пользователь
        filmDbStorage.addMark(film1Id, user2Id, 10);
        filmDbStorage.addMark(film4Id, user2Id, 4);
        filmDbStorage.addMark(film6Id, user2Id, 8);
        // фильм для рекомендации
        filmDbStorage.addMark(film7Id, user2Id, 6);
        film7.setMark(6.0);
        // фильм для рекомендации
        filmDbStorage.addMark(film8Id, user2Id, 10);
        film8.setMark(10.0);
        List<Film> films = new ArrayList<>();
        films.add(film7);
        films.add(film8);

        filmDbStorage.addMark(film1Id, user3Id, 5);
        filmDbStorage.addMark(film2Id, user3Id, 10);
        filmDbStorage.addMark(film4Id, user3Id, 10);

        filmDbStorage.addMark(film5Id, user4Id, 2);
        filmDbStorage.addMark(film9Id, user4Id, 10);
        filmDbStorage.addMark(film10Id, user4Id, 7);
        filmDbStorage.addMark(film4Id, user4Id, 10);

        filmDbStorage.addMark(film2Id, user5Id, 3);
        filmDbStorage.addMark(film5Id, user5Id, 7);
        filmDbStorage.addMark(film9Id, user5Id, 10);

        filmDbStorage.addMark(film9Id, user6Id, 10);
        filmDbStorage.addMark(film7Id, user6Id, 6);
        filmDbStorage.addMark(film3Id, user6Id, 9);

        List<Film> recommendations = userDbStorage.getRecommendations(user1Id);

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        assertTrue(recommendations.containsAll(films));
    }

    @Test
    @DisplayName("Удаление пользователя")
    void testDeleteUser() {
        userDbStorage.addUser(userOne);
        userDbStorage.deleteUser(1);
        assertEquals(0, userDbStorage.getAllUsers().size());
    }
}