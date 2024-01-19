package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private static final Integer MPA_ID = 1;
    private static final String MPA_NAME = "G";
    private static final Integer GENRE_ID = 1;
    private static final String GENRE_NAME = "Комедия";
    private static Genre genreOne = new Genre();
    private static final String NAME_FILM_ONE = "Тестовый фильм";
    private static final String DESCRIPTION = "Описание тестового фильма";
    private static final LocalDate RELEASE_DATE_FILM_ONE = LocalDate.of(1986, 10, 25);
    private static final Integer DURATION_FILM_ONE = 120;
    private static final Mpa mpa = new Mpa();
    private static final Integer INITIAL_LIKE = 0;
    private static Film filmOne = null;
    private FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeAll
    static void beforeAll() {
        mpa.setId(MPA_ID);
        mpa.setName(MPA_NAME);
        genreOne.setId(GENRE_ID);
        genreOne.setName(GENRE_NAME);
        List<Genre> genres = new ArrayList<>();
        genres.add(genreOne);
        filmOne = new Film(NAME_FILM_ONE, DESCRIPTION, RELEASE_DATE_FILM_ONE, DURATION_FILM_ONE, mpa);
        filmOne.setLike(INITIAL_LIKE);
        filmOne.setGenres(genres);
    }

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    @DisplayName("Добавление фильма")
    void testAddFilmShouldBeEquals() {
        int expectedId = 1;

        filmDbStorage.addFilm(filmOne);
        Film savedFilm = filmDbStorage.getFilmById(expectedId).get(0);

        assertEquals(filmOne, savedFilm);
    }

    @Test
    void updateFilm() {
    }

    @Test
    void getFilmById() {
    }

    @Test
    void getAllFilms() {
    }

    @Test
    void addLike() {
    }

    @Test
    void deleteLike() {
    }

    @Test
    void getPopularFilms() {
    }
}