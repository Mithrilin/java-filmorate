package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private static final Integer MPA_ID_ONE = 1;
    private static final String MPA_NAME_ONE = "G";
    private static final Integer MPA_ID_TWO = 2;
    private static final String MPA_NAME_TWO = "PG";
    private static final Integer GENRE_ID_ONE = 1;
    private static final String GENRE_NAME_ONE = "Комедия";
    private static final Integer GENRE_ID_TWO = 2;
    private static final String GENRE_NAME_TWO = "Драма";
    private static final Genre genreOne = new Genre();
    private static final Genre genreTwo = new Genre();
    private static final String NAME_FILM_ONE = "Тестовый фильм один";
    private static final String NAME_FILM_TWO = "Тестовый фильм два";
    private static final String DESCRIPTION_FILM_ONE = "Описание первого тестового фильма";
    private static final String DESCRIPTION_FILM_TWO = "Описание второго тестового фильма";
    private static final LocalDate RELEASE_DATE_FILM_ONE = LocalDate.of(1986, 10, 25);
    private static final LocalDate RELEASE_DATE_FILM_TWO = LocalDate.of(1974, 03, 14);
    private static final Integer DURATION_FILM_ONE = 120;
    private static final Integer DURATION_FILM_TWO = 100;
    private static final Mpa mpaOne = new Mpa();
    private static final Mpa mpaTwo = new Mpa();
    private static final Integer INITIAL_LIKE = 0;
    private static Film filmOne = null;
    private static Film filmTwo = null;
    private static final LocalDate BIRTHDAY_USER_ONE = LocalDate.of(1986, 10, 25);
    private static final String NAME_USER_ONE = "Nick Name";
    private static final String LOGIN_USER_ONE = "dolore";
    private static final String EMAIL_USER_ONE = "dolore@mail.ru";
    private static User userOne = null;
    private FilmDbStorage filmDbStorage;
    private UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        userDbStorage = new UserDbStorage(jdbcTemplate);

        mpaOne.setId(MPA_ID_ONE);
        mpaOne.setName(MPA_NAME_ONE);
        genreOne.setId(GENRE_ID_ONE);
        genreOne.setName(GENRE_NAME_ONE);
        List<Genre> genresFilmOne = new ArrayList<>();
        genresFilmOne.add(genreOne);
        filmOne = new Film(NAME_FILM_ONE, DESCRIPTION_FILM_ONE, RELEASE_DATE_FILM_ONE, DURATION_FILM_ONE, mpaOne);
        filmOne.setGenres(genresFilmOne);

        mpaTwo.setId(MPA_ID_TWO);
        mpaTwo.setName(MPA_NAME_TWO);
        genreTwo.setId(GENRE_ID_TWO);
        genreTwo.setName(GENRE_NAME_TWO);
        List<Genre> genresFilmTwo = new ArrayList<>();
        genresFilmTwo.add(genreTwo);
        filmTwo = new Film(NAME_FILM_TWO, DESCRIPTION_FILM_TWO, RELEASE_DATE_FILM_TWO, DURATION_FILM_TWO, mpaOne);
        filmTwo.setGenres(genresFilmTwo);

        userOne = new User(EMAIL_USER_ONE, LOGIN_USER_ONE, NAME_USER_ONE, BIRTHDAY_USER_ONE);
    }

    @Test
    @DisplayName("Добавление фильма")
    void testAddFilmShouldBeEquals() {
        filmOne.setLike(INITIAL_LIKE);

        int filmId = filmDbStorage.addFilm(filmOne).getId();
        Film savedFilm = filmDbStorage.getFilmById(filmId).get(0);

        assertNotNull(savedFilm);
        assertEquals(filmOne, savedFilm);
    }

    @Test
    @DisplayName("Обновление фильма")
    void testUpdateFilmShouldBeEquals() {
        Film savedFilm = filmDbStorage.addFilm(filmOne);
        filmTwo.setId(savedFilm.getId());
        filmTwo.setLike(INITIAL_LIKE);

        filmDbStorage.updateFilm(filmTwo);
        Film updatedFilm = filmDbStorage.getFilmById(savedFilm.getId()).get(0);

        assertNotNull(updatedFilm);
        assertEquals(filmTwo, updatedFilm);
    }

    @Test
    @DisplayName("Обновление фильма с пустым списком жанров")
    void testUpdateFilmShouldBe0WhenGenresIsEmpty() {
        Film savedFilm = filmDbStorage.addFilm(filmOne);
        filmTwo.setGenres(new ArrayList<>());
        filmTwo.setId(savedFilm.getId());
        filmTwo.setLike(INITIAL_LIKE);

        filmDbStorage.updateFilm(filmTwo);
        Film updatedFilm = filmDbStorage.getFilmById(savedFilm.getId()).get(0);

        assertNotNull(updatedFilm);
        assertEquals(0, updatedFilm.getGenres().size());
    }

    @Test
    @DisplayName("Ошибка обновление фильма, когда неправильный ид")
    void testUpdateFilmShouldThrowExceptionWhenWrongId() {
        int wrongId = 999;
        filmDbStorage.addFilm(filmOne);
        filmTwo.setId(wrongId);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmDbStorage.updateFilm(filmTwo));

        assertEquals("Фильм с id " + wrongId + " не найден.", exception.getMessage());
    }

    @Test
    @DisplayName("Пустой список с пользователями, когда неправильный ид")
    void testGetFilmByIdShouldBe0WhenWrongId() {
        int wrongId = 999;
        filmDbStorage.addFilm(filmOne);

        List<Film> films = filmDbStorage.getFilmById(wrongId);

        assertEquals(0, films.size());
    }

    @Test
    @DisplayName("Получение всех фильмов")
    void testGetAllFilmsShouldBeEquals() {
        List<Film> films = new ArrayList<>();
        films.add(filmOne);
        films.add(filmTwo);
        int filmOneId = filmDbStorage.addFilm(filmOne).getId();
        filmOne.setId(filmOneId);
        int filmTwoId = filmDbStorage.addFilm(filmTwo).getId();
        filmTwo.setId(filmTwoId);

        List<Film> returnedFilms = filmDbStorage.getAllFilms();

        assertNotNull(returnedFilms);
        assertEquals(films, returnedFilms);
    }

    @Test
    @DisplayName("Получение всех фильмов с 1 лайком в фильме 1")
    void testGetAllFilmsShouldBe1() {
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        int userId = userDbStorage.addUser(userOne).getId();
        filmDbStorage.addLike(filmId, userId);

        List<Film> returnedFilms = filmDbStorage.getAllFilms();

        assertEquals(2, returnedFilms.size());
        assertEquals(1, returnedFilms.get(0).getLike());
    }

    @Test
    @DisplayName("Добавление лайка к фильму")
    void testAddLikeShouldBeEquals() {
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int userId = userDbStorage.addUser(userOne).getId();

        filmDbStorage.addLike(filmId, userId);

        Film film = filmDbStorage.getFilmById(filmId).get(0);
        assertEquals(1, film.getLike());
    }

    @Test
    @DisplayName("Удаление лайка")
    void testDeleteLikeShouldBeEquals() {
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int userId = userDbStorage.addUser(userOne).getId();

        filmDbStorage.addLike(filmId, userId);
        filmDbStorage.deleteLike(filmId, userId);

        Film film = filmDbStorage.getFilmById(filmId).get(0);
        assertEquals(0, film.getLike());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов")
    void testGetAllPopularFilmsShouldBe2WhenCountIsNull() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        filmOne.setId(filmId);
        filmDbStorage.addLike(filmId, userId);

        List<Film> films = filmDbStorage.getPopularFilms(null);

        assertEquals(2, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение самого популярного фильма")
    void testGetMostPopularFilmShouldBe1WhenCountIs1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        filmOne.setId(filmId);
        filmDbStorage.addLike(filmId, userId);

        List<Film> films = filmDbStorage.getPopularFilms("1");

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
        assertEquals(1, films.get(0).getLike());
    }

    @Test
    @DisplayName("Получение списка общих фильмов")
    void testGetCommonFilms() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        userDbStorage.addUser(new User("ford@ya.ru", "ford", "Ford", BIRTHDAY_USER_ONE));
        filmDbStorage.addLike(1, 1);
        filmDbStorage.addLike(2, 1);
        filmDbStorage.addLike(2, 2);
        filmTwo.setLike(2);
        List<Film> films = filmDbStorage.getCommonFilms(1, 2);
        assertEquals(1, films.size());
        assertEquals(filmTwo, films.get(0));
    }

    @Test
    @DisplayName("Получение пустого списка общих фильмов")
    void testGetCommonFilmsEmpty() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        userDbStorage.addUser(new User("ford@ya.ru", "ford", "Ford", BIRTHDAY_USER_ONE));
        filmDbStorage.addLike(1, 1);
        filmDbStorage.addLike(2, 2);
        List<Film> films = filmDbStorage.getCommonFilms(1, 2);
        assertEquals(0, films.size());
    }
}