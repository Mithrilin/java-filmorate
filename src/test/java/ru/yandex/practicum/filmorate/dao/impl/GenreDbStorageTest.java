package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
    private static final Integer GENRE_ID_ONE = 1;
    private static final String GENRE_NAME_ONE = "Комедия";
    private static Genre genreOne = null;
    private GenreDbStorage genreDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        genreDbStorage = new GenreDbStorage(jdbcTemplate);
        genreOne = new Genre(GENRE_ID_ONE, GENRE_NAME_ONE);
    }

    @Test
    @DisplayName("Получение жанра по ид")
    void testGetGenreByIdShouldBeEquals() {
        Genre genre = genreDbStorage.getGenreById(GENRE_ID_ONE).get(0);

        assertEquals(genreOne, genre);
    }

    @Test
    @DisplayName("Получение списка всех жанров")
    void testGetAllGenresShouldBe6() {
        List<Genre> genres = genreDbStorage.getAllGenres();

        assertNotNull(genres);
        assertEquals(6, genres.size());
    }
}