package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository("directorDbStorage")
public class DirectorDbStorage implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
                .withTableName("directors")
                .usingGeneratedKeyColumns("director_id");
        Map<String, String> params = Map.of(
                "director_name", director.getName());
        director.setId(simpleJdbcInsert.executeAndReturnKey(params).intValue());
        return director;
    }

    @Override
    public List<Director> getDirectors() {
        return jdbcTemplate.query("SELECT * " +
                                      "FROM directors " +
                                      "ORDER BY director_id", directorRowMapper());
    }

    @Override
    public List<Director> getDirectorById(int id) {
        return jdbcTemplate.query("SELECT * " +
                                      "FROM directors " +
                                      "WHERE director_id = ?", directorRowMapper(), id);
    }

    @Override
    public Director updateDirector(Director director) {
        int result = jdbcTemplate.update("update directors set director_name = ? where director_id = ?;",
                director.getName(), director.getId());
        // Проверка на наличие директора в БД
        if (result == 0) {
            return null;
        }
        return director;
    }

    @Override
    public int deleteDirector(int id) {
        return jdbcTemplate.update("DELETE FROM public.directors WHERE director_id = ?", id);
    }

    private RowMapper<Director> directorRowMapper() {
        return (rs, rowNum) -> new Director(rs.getInt("director_id"),
                rs.getString("director_name"));
    }
}

