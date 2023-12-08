package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmServiceImpl;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        filmController = new FilmController(new FilmServiceImpl(new InMemoryFilmStorage()));
    }

    @Test
    @DisplayName("Успешное создание фильма с ID 1")
    void shouldReturn1WhenValidFilmCreate() {
        int expectedId = 1;
        Integer duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Film film = filmController.createFilm(new Film("Тестовый фильм", "Описание", releaseDate, duration));

        assertEquals(expectedId, film.getId());
    }

    @Test
    @DisplayName("Возвращение списка фильмов")
    void shouldBeEqualsWhenValidFilm() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Film film = new Film("Тестовый фильм", "Описание", releaseDate, duration);
        filmController.createFilm(film);
        List<Film> filmList = filmController.findAllFilms();

        assertEquals(film, filmList.get(0));
    }

    @Test
    @DisplayName("Обновление фильма")
    void shouldBeNotEqualsWhenValidFilmUpdate() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Film film = filmController.createFilm(new Film("Тестовый фильм", "Описание", releaseDate, duration));
        Film updateFilm = new Film("Обновлённый фильм", "Описание", releaseDate, duration);
        updateFilm.setId(film.getId());
        filmController.updateFilm(updateFilm);
        List<Film> filmList = filmController.findAllFilms();

        assertNotEquals(film, filmList.get(0));
    }

    @Test
    @DisplayName("Пустое имя фильма ")
    void shouldBeFalseWhenNameIsBlank() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Set<ConstraintViolation<Film>> violations = validator.validate(
                new Film("", "Описание", releaseDate, duration));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Null вместо имени фильма ")
    void shouldBeFalseWhenNameIsNull() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Set<ConstraintViolation<Film>> violations = validator.validate(
                new Film(null, "Описание", releaseDate, duration));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Длина описания больше 200 ")
    void shouldBeFalseWhenDescriptionSizeIs201() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        String description = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "a";
        Set<ConstraintViolation<Film>> violations = validator.validate(
                new Film("Тестовый фильм", description, releaseDate, duration));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Продолжительность отрицательная")
    void shouldBeFalseWhenDurationIsNegative() {
        int duration = -120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        Set<ConstraintViolation<Film>> violations = validator.validate(
                new Film("Тестовый фильм", "Описание", releaseDate, duration));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Дата релиза меньше минимальной даты")
    void shouldThrowExceptionWhenWrongReleaseDate() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1786, 10, 25);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(new Film("Тестовый фильм", "Описание", releaseDate,
                        duration)));

        assertEquals("Фильм не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("ID у фильма отсутствует при обновлении")
    void shouldThrowExceptionWhenWrongId() {
        int duration = 120;
        LocalDate releaseDate = LocalDate.of(1986, 10, 25);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.updateFilm(new Film("Тестовый фильм", "Описание", releaseDate,
                        duration)));

        assertEquals("Фильм не прошёл валидацию.", exception.getMessage());
    }
}