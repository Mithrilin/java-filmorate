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

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
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
    private FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;


    @BeforeAll
    static void beforeAll() {
        mpaOne.setId(MPA_ID_ONE);
        mpaOne.setName(MPA_NAME_ONE);
        genreOne.setId(GENRE_ID_ONE);
        genreOne.setName(GENRE_NAME_ONE);
        List<Genre> genresFilmOne = new ArrayList<>();
        genresFilmOne.add(genreOne);
        filmOne = new Film(NAME_FILM_ONE, DESCRIPTION_FILM_ONE, RELEASE_DATE_FILM_ONE, DURATION_FILM_ONE, mpaOne);
        filmOne.setLike(INITIAL_LIKE);
        filmOne.setGenres(genresFilmOne);

        mpaTwo.setId(MPA_ID_TWO);
        mpaTwo.setName(MPA_NAME_TWO);
        genreTwo.setId(GENRE_ID_TWO);
        genreTwo.setName(GENRE_NAME_TWO);
        List<Genre> genresFilmTwo = new ArrayList<>();
        genresFilmTwo.add(genreTwo);
        filmTwo = new Film(NAME_FILM_TWO, DESCRIPTION_FILM_TWO, RELEASE_DATE_FILM_TWO, DURATION_FILM_TWO, mpaOne);
        filmTwo.setLike(INITIAL_LIKE);
        filmTwo.setGenres(genresFilmTwo);
    }

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
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

    @Test
    @DisplayName("Добавление фильма")
    void testAddFilmShouldBeEquals() {
        int filmId = 1;

        filmDbStorage.addFilm(filmOne);
        Film savedFilm = filmDbStorage.getFilmById(filmId).get(0);

        assertNotNull(savedFilm);
        assertEquals(filmOne, savedFilm);
    }

    @Test
    @DisplayName("Обновление фильма")
    void testUpdateFilmShouldBeEquals() {
        Film savedFilm = filmDbStorage.addFilm(filmOne);
        filmTwo.setId(savedFilm.getId());

        filmDbStorage.updateFilm(filmTwo);
        Film updatedFilm = filmDbStorage.getFilmById(savedFilm.getId()).get(0);

        assertNotNull(updatedFilm);
        assertEquals(filmTwo, updatedFilm);
    }
}