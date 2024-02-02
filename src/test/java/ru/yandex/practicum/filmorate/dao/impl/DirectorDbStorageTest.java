package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {

    private DirectorDbStorage directorDbStorage;
    private final JdbcTemplate jdbcTemplate;

    private Director directorOne;
    private Director directorOneUp;
    private Director directorTwo;

    @BeforeEach
    void setUp() {

        directorDbStorage = new DirectorDbStorage(jdbcTemplate);

        directorOne = new Director(1,"Director");

        directorOneUp = new Director(1,"Director one up");

        directorTwo = new Director(2,"Director two");

    }
    @Test
    @DisplayName("постман тесты")
    void getDirectorOneBeforeCreate() {

        //   directorDbStorage.getDirectorById(1);
    }

    @Test
    @DisplayName("Добавление, получение, обновление, удаление режиссера")
    void createAndGetAndUpdateAndDeleteDirectors() {
        assertEquals(directorOne, directorDbStorage.addDirector(directorOne),"добавить режиссера Director");
        assertEquals(directorTwo, directorDbStorage.addDirector(directorTwo),"добавить режиссера Director two");
        assertEquals(directorOne, directorDbStorage.getDirectorById(1),"вернуть режиссера Director");
        assertIterableEquals(
                List.of(directorOne,directorTwo),directorDbStorage.getDirectors(), "вернуть всех режиссеров"
        );
        assertEquals(
                directorOneUp, directorDbStorage.updateDirector(directorOneUp),"обновить режиссера Director"
        );
        assertIterableEquals(
                List.of(directorOneUp,directorTwo),directorDbStorage.getDirectors(), "вернуть всех режиссеров"
        );

        directorDbStorage.deleteDirector(1);
        assertIterableEquals(
                List.of(directorTwo),directorDbStorage.getDirectors(),
                "вернуть всех режиссеров после удаления режиссера Director one up"
        );

    }



}