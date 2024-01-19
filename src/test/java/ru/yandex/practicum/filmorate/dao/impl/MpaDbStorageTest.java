package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {
    private static final Integer MPA_ID_ONE = 1;
    private static final String MPA_NAME_ONE = "G";
    private static Mpa mpaOne = null;
    private MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mpaDbStorage = new MpaDbStorage(jdbcTemplate);
        mpaOne = new Mpa(MPA_ID_ONE, MPA_NAME_ONE);
    }

    @Test
    @DisplayName("Получение рейтинга по ид")
    void testGetMpaByIdShouldBeEquals() {
        Mpa mpa = mpaDbStorage.getMpaById(MPA_ID_ONE).get(0);

        assertEquals(mpaOne, mpa);
    }

    @Test
    @DisplayName("Получение списка с рейтингами")
    void testGetAllMpaShouldBe5() {
        List<Mpa> mpas = mpaDbStorage.getAllMpa();

        assertNotNull(mpas);
        assertEquals(5, mpas.size());
    }
}