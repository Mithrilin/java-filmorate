package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;

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

    private static final Director directorOne = new Director(1, "Director one");
    private static final Director directorTwo = new Director(2, "Director two");
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
    private DirectorDbStorage directorDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        userDbStorage = new UserDbStorage(jdbcTemplate);
        directorDbStorage = new DirectorDbStorage(jdbcTemplate);

        directorDbStorage.addDirector(directorOne);
        directorDbStorage.addDirector(directorTwo);

        mpaOne.setId(MPA_ID_ONE);
        mpaOne.setName(MPA_NAME_ONE);
        genreOne.setId(GENRE_ID_ONE);
        genreOne.setName(GENRE_NAME_ONE);
        List<Genre> genresFilmOne = new ArrayList<>();
        genresFilmOne.add(genreOne);
        filmOne = new Film(NAME_FILM_ONE,
                DESCRIPTION_FILM_ONE,
                RELEASE_DATE_FILM_ONE,
                DURATION_FILM_ONE,
                mpaOne);
        filmOne.setGenres(genresFilmOne);
        filmOne.setDirectors(List.of(directorOne, directorTwo));

        mpaTwo.setId(MPA_ID_TWO);
        mpaTwo.setName(MPA_NAME_TWO);
        genreTwo.setId(GENRE_ID_TWO);
        genreTwo.setName(GENRE_NAME_TWO);
        List<Genre> genresFilmTwo = new ArrayList<>();
        genresFilmTwo.add(genreTwo);
        filmTwo = new Film(NAME_FILM_TWO,
                DESCRIPTION_FILM_TWO,
                RELEASE_DATE_FILM_TWO,
                DURATION_FILM_TWO,
                mpaOne);
        filmTwo.setGenres(genresFilmTwo);
        filmTwo.setDirectors(List.of(directorTwo));
        userOne = new User(EMAIL_USER_ONE, LOGIN_USER_ONE, NAME_USER_ONE, BIRTHDAY_USER_ONE);

    }

    @Test
    @DisplayName("Добавление фильма")
    void testAddFilmShouldBeEquals() {
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

        filmDbStorage.updateFilm(filmTwo);
        Film updatedFilm = filmDbStorage.getFilmById(savedFilm.getId()).get(0);

        assertNotNull(updatedFilm);
        assertEquals(0, updatedFilm.getGenres().size());
    }

    @Test
    @DisplayName("Нулловый фильм, когда неправильный ид")
    void testUpdateFilmShouldBeNullWhenWrongId() {
        int wrongId = 999;
        filmDbStorage.addFilm(filmOne);
        filmTwo.setId(wrongId);

        Film film = filmDbStorage.updateFilm(filmTwo);

        assertNull(film);
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
        filmDbStorage.addMark(filmId, userId, "6");

        List<Film> returnedFilms = filmDbStorage.getAllFilms();

        assertEquals(2, returnedFilms.size());
        assertEquals(6, returnedFilms.get(0).getMark());
    }

    @Test
    @DisplayName("Добавление оценки к фильму")
    void testAddLikeShouldBeEquals() {
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int userId = userDbStorage.addUser(userOne).getId();
        int userIdTwo = userDbStorage.addUser(new User("222@mail.ru", "22222", "2222", BIRTHDAY_USER_ONE)).getId();

        filmDbStorage.addMark(filmId, userId, "10");
        filmDbStorage.addMark(filmId, userIdTwo, "6");

        Film film = filmDbStorage.getFilmById(filmId).get(0);
        assertEquals(8, film.getMark());
    }

    @Test
    @DisplayName("Удаление лайка")
    void testDeleteLikeShouldBeEquals() {
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int userId = userDbStorage.addUser(userOne).getId();

        filmDbStorage.addMark(filmId, userId, "10");
        filmDbStorage.deleteMark(filmId, userId);

        Film film = filmDbStorage.getFilmById(filmId).get(0);
        assertEquals(0, film.getMark());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов")
    void testGetAllPopularFilmsShouldBe2WhenCountIsNull() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int filmTwoId = filmDbStorage.addFilm(filmTwo).getId();
        filmDbStorage.addFilm(new Film("333", "333", RELEASE_DATE_FILM_ONE, 100, mpaOne));
        filmOne.setId(filmId);
        filmDbStorage.addMark(filmId, userId, "10");
        filmDbStorage.addMark(filmTwoId, userId, "6");

        List<Film> films = filmDbStorage.getPopularFilms();

        assertEquals(3, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение самого популярного фильма")
    void testGetMostPopularFilmShouldBe1WhenCountIs1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        filmOne.setId(filmId);
        filmDbStorage.addMark(filmId, userId, "7");

        List<Film> films = filmDbStorage.getPopularFilmsWithLimit(1);

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
        assertEquals(7, films.get(0).getMark());
    }

    @Test
    @DisplayName("Получение списка общих фильмов")
    void testGetCommonFilms() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        userDbStorage.addUser(new User("ford@ya.ru", "ford", "Ford", BIRTHDAY_USER_ONE));
        filmDbStorage.addMark(1, 1, "6");
        filmDbStorage.addMark(2, 1, "8");
        filmDbStorage.addMark(2, 2, "10");

        List<Film> films = filmDbStorage.getCommonFilms(1, 2);

        assertEquals(1, films.size());
        assertEquals(2, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение пустого списка общих фильмов")
    void testGetCommonFilmsEmpty() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        userDbStorage.addUser(new User("ford@ya.ru", "ford", "Ford", BIRTHDAY_USER_ONE));
        filmDbStorage.addMark(1, 1, "10");
        filmDbStorage.addMark(2, 1, "1");
        filmDbStorage.addMark(2, 2, "10");
        List<Film> films = filmDbStorage.getCommonFilms(1, 2);
        assertEquals(0, films.size());
    }

    @Test
    @DisplayName("Удаление фильма")
    void testDeleteFilm() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.deleteFilm(1);
        assertEquals(0, filmDbStorage.getAllFilms().size());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов с жанром 1")
    void testGetAllPopularFilmsWithGenreId1ShouldBe1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        int filmIdTwo = filmDbStorage.addFilm(filmTwo).getId();
        filmOne.setId(filmId);
        filmDbStorage.addMark(filmId, userId, "10");
        filmDbStorage.addMark(filmIdTwo, userId, "8");

        List<Film> films = filmDbStorage.getPopularFilmsByGenre(1);

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов с жанром 1 с лимитом 1")
    void testGetAllPopularFilmsWithGenreId1AndLimit1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmTwo.getGenres().add(genreOne);
        int filmIdTwo = filmDbStorage.addFilm(filmTwo).getId();
        filmDbStorage.addMark(filmId, userId, "10");
        filmDbStorage.addMark(filmIdTwo, userId, "8");

        List<Film> films = filmDbStorage.getPopularFilmsByGenreWithLimit(1, 1);

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов 1986 года")
    void testGetAllPopularFilmsWithYear1986ShouldBe1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        filmOne.setId(filmId);
        filmDbStorage.addMark(filmId, userId, "10");

        List<Film> films = filmDbStorage.getPopularFilmsByYear(1986);

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Получение всех популярных фильмов 1986 года и жанра 1")
    void testGetAllPopularFilmsWithYear1986AndGenreId1ShouldBe1() {
        int userId = userDbStorage.addUser(userOne).getId();
        int filmId = filmDbStorage.addFilm(filmOne).getId();
        filmDbStorage.addFilm(filmTwo);
        filmOne.setId(filmId);
        filmDbStorage.addMark(filmId, userId, "10");

        List<Film> films = filmDbStorage.getPopularFilmsByYearAndGenre(1, 1986);

        assertEquals(1, films.size());
        assertEquals(filmId, films.get(0).getId());
    }

    @Test
    @DisplayName("Поиск фильма по названию")
    void testSearchByTitle() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        filmDbStorage.addMark(2, 1, "10");
        List<Film> films = filmDbStorage.getFilmsByTitleSearch("фильм");
        assertEquals(2, films.size());
        assertEquals(10, films.get(0).getMark());
    }

    @Test
    @DisplayName("Поиск фильма по неправильному названию")
    void testSearchByWrongTitle() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);

        List<Film> films = filmDbStorage.getFilmsByTitleSearch("pbvf");

        assertEquals(0, films.size());
    }

    @Test
    @DisplayName("Поиск фильма по названию и режиссеру")
    void testSearchByTitleAndDirector() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        filmDbStorage.addMark(2, 1, "10");

        List<Film> films = filmDbStorage.getFilmsByTitleAndDirectorSearch("dir");

        assertEquals(2, films.size());
        assertEquals(10, films.get(0).getMark());
    }

    @Test
    @DisplayName("Поиск фильма по неправильному названию и режиссеру")
    void testSearchByWrongTitleAndDirector() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);

        List<Film> films = filmDbStorage.getFilmsByTitleAndDirectorSearch("pbvf");

        assertEquals(0, films.size());
    }

    @Test
    @DisplayName("Поиск фильма по режиссеру")
    void testSearchByDirector() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);
        filmDbStorage.addMark(2, 1, "10");

        List<Film> films = filmDbStorage.getFilmsByDirectorSearch("dir");

        assertEquals(2, films.size());
        assertEquals(10, films.get(0).getMark());
    }

    @Test
    @DisplayName("Поиск фильма по неправильному режиссеру")
    void testSearchByWrongDirector() {
        filmDbStorage.addFilm(filmOne);
        filmDbStorage.addFilm(filmTwo);
        userDbStorage.addUser(userOne);

        List<Film> films = filmDbStorage.getFilmsByDirectorSearch("pbvf");

        assertEquals(0, films.size());
    }
}