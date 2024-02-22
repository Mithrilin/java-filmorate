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
import ru.yandex.practicum.filmorate.service.event.EventServiceImpl;
import ru.yandex.practicum.filmorate.service.user.UserServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RecommendationsTest {
    private static final Integer MPA_ID = 1;
    private static final String MPA_NAME = "G";
    private static final Integer DURATION_FILM = 120;
    private static final LocalDate BIRTHDAY_USER = LocalDate.of(1986, 10, 25);
    private static final LocalDate RELEASE_DATE_FILM = LocalDate.of(1986, 10, 25);
    private UserDbStorage userDbStorage;
    private UserServiceImpl userServiceImpl;
    private FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        userDbStorage = new UserDbStorage(jdbcTemplate);
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        userServiceImpl = new UserServiceImpl(userDbStorage, filmDbStorage, new EventServiceImpl(new EventDbStorage(jdbcTemplate)));
    }

    @Test
    @DisplayName("Получение рекомендаций")
    void testGetRecommendations_ShouldBeEquals() {
        List<User> users = usersBuilder();
        List<Film> films = filmsBuilder();

        List<Integer> usersId = users.stream()
                .map(user -> userDbStorage.addUser(user).getId())
                .collect(Collectors.toList());
        List<Integer> filmsId = films.stream()
                .map(film -> filmDbStorage.addFilm(film).getId())
                .collect(Collectors.toList());

        // Целевой пользователь
        filmDbStorage.addMark(filmsId.get(0), usersId.get(0), 1);
        filmDbStorage.addMark(filmsId.get(1), usersId.get(0), 1);
        filmDbStorage.addMark(filmsId.get(2), usersId.get(0), 3);
        filmDbStorage.addMark(filmsId.get(3), usersId.get(0), 3);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(0), 5);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(0), 5);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(0), 7);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(0), 7);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(0), 9);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(0), 9);

        // Максимально похожий по оценкам пользователь
        // Совпадение 9 из 10 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(1), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(2), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(3), usersId.get(1), 4);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(1), 4);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(1), 6);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(1), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(1), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(1), 8);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(1), 10);
        // фильмы для рекомендации
        filmDbStorage.addMark(filmsId.get(10), usersId.get(1), 10);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(1), 8);
        // фильм, который не попадёт в рекомендации
        filmDbStorage.addMark(filmsId.get(12), usersId.get(1), 1);

        // остальные пользователи
        // Совпадений 7 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(3), usersId.get(2), 2);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(2), 4);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(2), 6);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(2), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(2), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(2), 10);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(2), 8);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(2), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(2), 7);
        // Совпадений 5 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(5), usersId.get(3), 3);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(3), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(3), 10);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(3), 6);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(3), 4);
        // Совпадений 3 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(7), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(4), 10);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(4), 7);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(4), 2);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(4), 1);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(4), 7);
        // Совпадений 3 с разницей 0 на каждый фильм
        filmDbStorage.addMark(filmsId.get(7), usersId.get(5), 7);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(5), 9);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(5), 9);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(5), 10);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(5), 8);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(5), 6);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(5), 4);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(5), 2);
        // Совпадений 1 с разницей 0
        filmDbStorage.addMark(filmsId.get(9), usersId.get(6), 9);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(6), 7);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(6), 6);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(6), 5);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(6), 7);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(6), 9);

        List<Film> recommendations = userServiceImpl.getRecommendations(usersId.get(0));

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        assertEquals(filmsId.get(10), recommendations.get(0).getId());
        assertEquals(filmsId.get(11), recommendations.get(1).getId());
    }

    @Test
    @DisplayName("Получение рекомендаций, когда у похожего пользователя нет других фильмов")
    void testGetRecommendations_WhenSimilarUserHaveNotFilmsForRecommendations() {
        List<User> users = usersBuilder();
        List<Film> films = filmsBuilder();

        List<Integer> usersId = users.stream()
                .map(user -> userDbStorage.addUser(user).getId())
                .collect(Collectors.toList());
        List<Integer> filmsId = films.stream()
                .map(film -> filmDbStorage.addFilm(film).getId())
                .collect(Collectors.toList());

        // Целевой пользователь
        filmDbStorage.addMark(filmsId.get(0), usersId.get(0), 1);
        filmDbStorage.addMark(filmsId.get(1), usersId.get(0), 1);
        filmDbStorage.addMark(filmsId.get(2), usersId.get(0), 3);
        filmDbStorage.addMark(filmsId.get(3), usersId.get(0), 3);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(0), 5);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(0), 5);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(0), 7);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(0), 7);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(0), 9);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(0), 9);

        // Максимально похожий по оценкам пользователь
        // Совпадение 9 из 10 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(1), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(2), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(3), usersId.get(1), 4);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(1), 4);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(1), 6);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(1), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(1), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(1), 8);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(1), 10);

        // Второй похожий пользователь
        // Совпадений 7 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(3), usersId.get(2), 2);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(2), 4);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(2), 6);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(2), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(2), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(2), 10);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(2), 8);
        // фильмы для рекомендации
        filmDbStorage.addMark(filmsId.get(10), usersId.get(2), 10);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(2), 8);
        // фильм, который не попадёт в рекомендации
        filmDbStorage.addMark(filmsId.get(12), usersId.get(2), 1);

        // остальные пользователи
        // Совпадений 5 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(5), usersId.get(3), 3);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(3), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(3), 10);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(3), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(3), 6);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(3), 4);
        // Совпадений 3 с разницей по 1 на каждый фильм
        filmDbStorage.addMark(filmsId.get(7), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(4), 10);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(4), 7);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(4), 2);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(4), 1);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(4), 7);
        // Совпадений 3 с разницей 0 на каждый фильм
        filmDbStorage.addMark(filmsId.get(7), usersId.get(5), 7);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(5), 9);
        filmDbStorage.addMark(filmsId.get(9), usersId.get(5), 9);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(5), 10);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(5), 8);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(5), 6);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(5), 4);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(5), 2);
        // Совпадений 1 с разницей 0
        filmDbStorage.addMark(filmsId.get(9), usersId.get(6), 9);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(6), 7);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(6), 6);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(6), 5);
        filmDbStorage.addMark(filmsId.get(13), usersId.get(6), 7);
        filmDbStorage.addMark(filmsId.get(14), usersId.get(6), 9);

        List<Film> recommendations = userServiceImpl.getRecommendations(usersId.get(0));

        assertNotNull(recommendations);
        assertEquals(2, recommendations.size());
        assertEquals(filmsId.get(10), recommendations.get(0).getId());
        assertEquals(filmsId.get(11), recommendations.get(1).getId());
    }

    @Test
    @DisplayName("Получение рекомендаций, когда оценки ни с кем не совпадают")
    void testGetRecommendations_ShouldBe5WhenNoMatches() {
        List<User> users = usersBuilder();
        List<Film> films = filmsBuilder();

        List<Integer> usersId = users.stream()
                .map(user -> userDbStorage.addUser(user).getId())
                .collect(Collectors.toList());
        List<Integer> filmsId = films.stream()
                .map(film -> filmDbStorage.addFilm(film).getId())
                .collect(Collectors.toList());

        // Целевой пользователь
        filmDbStorage.addMark(filmsId.get(9), usersId.get(0), 9);

        // остальные пользователи
        filmDbStorage.addMark(filmsId.get(1), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(2), usersId.get(1), 2);
        filmDbStorage.addMark(filmsId.get(3), usersId.get(1), 4);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(1), 4);

        filmDbStorage.addMark(filmsId.get(3), usersId.get(2), 2);
        filmDbStorage.addMark(filmsId.get(4), usersId.get(2), 4);
        filmDbStorage.addMark(filmsId.get(5), usersId.get(2), 6);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(2), 6);

        filmDbStorage.addMark(filmsId.get(5), usersId.get(3), 3);
        filmDbStorage.addMark(filmsId.get(6), usersId.get(3), 6);
        filmDbStorage.addMark(filmsId.get(7), usersId.get(3), 8);

        filmDbStorage.addMark(filmsId.get(7), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(4), 10);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(4), 8);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(4), 7);

        filmDbStorage.addMark(filmsId.get(7), usersId.get(5), 7);
        filmDbStorage.addMark(filmsId.get(8), usersId.get(5), 9);
        filmDbStorage.addMark(filmsId.get(10), usersId.get(5), 10);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(5), 8);

        filmDbStorage.addMark(filmsId.get(10), usersId.get(6), 7);
        filmDbStorage.addMark(filmsId.get(11), usersId.get(6), 6);
        filmDbStorage.addMark(filmsId.get(12), usersId.get(6), 5);

        List<Film> recommendations = userServiceImpl.getRecommendations(usersId.get(0));
        assertNotNull(recommendations);
        assertEquals(5, recommendations.size());
    }

    @Test
    @DisplayName("Получение рекомендаций, когда только один пользователь")
    void testGetRecommendations_WhenOnlyOneUser() {
        List<User> users = usersBuilder();
        List<Film> films = filmsBuilder();
        int userId = userDbStorage.addUser(users.get(0)).getId();

        List<Integer> filmsId = films.stream()
                .map(film -> filmDbStorage.addFilm(film).getId())
                .collect(Collectors.toList());

        // Целевой пользователь
        filmDbStorage.addMark(filmsId.get(0), userId, 1);
        filmDbStorage.addMark(filmsId.get(1), userId, 1);
        filmDbStorage.addMark(filmsId.get(2), userId, 3);
        filmDbStorage.addMark(filmsId.get(3), userId, 3);
        filmDbStorage.addMark(filmsId.get(4), userId, 5);
        filmDbStorage.addMark(filmsId.get(5), userId, 5);
        filmDbStorage.addMark(filmsId.get(6), userId, 7);
        filmDbStorage.addMark(filmsId.get(7), userId, 7);
        filmDbStorage.addMark(filmsId.get(8), userId, 9);
        filmDbStorage.addMark(filmsId.get(9), userId, 9);

        List<Film> recommendations = userServiceImpl.getRecommendations(userId);
        assertNotNull(recommendations);
        assertEquals(5, recommendations.size());
    }

    private List<Film> filmsBuilder() {
        List<Film> films = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            films.add(
                    new Film(
                            "filmName " + i,
                            "description " + i,
                            RELEASE_DATE_FILM,
                            DURATION_FILM,
                            new Mpa(
                                    MPA_ID,
                                    MPA_NAME
                            )
                    )
            );
        }
        return films;
    }

    private List<User> usersBuilder() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i < 11; i++) {
            users.add(
                    new User(
                            i + "@mail.ru",
                            "log" + i,
                            "name" + i,
                            BIRTHDAY_USER
                    )
            );
        }
        return users;
    }
}
