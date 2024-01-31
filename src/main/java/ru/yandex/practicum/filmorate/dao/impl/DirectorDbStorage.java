package ru.yandex.practicum.filmorate.dao.impl;

import jdk.jshell.spi.ExecutionControl;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
@Component
public class DirectorDbStorage implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getDirectors() {
        return jdbcTemplate.query("select * from directors order by director_id;", directorRowMapper());
    }

    @Override
    public List<Director> getDirectorById(int id) {
        return jdbcTemplate.query("select * from directors where director_id = ?;", directorRowMapper(), id);
    }


    @Override
    public Director addDirector(Director director) {
        String sql = "insert into directors (director_name) values(?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

             jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql,new String[]{"director_id"});
                ps.setString(1,director.getName());
                return ps;
            },keyHolder);


        return getDirectorById((int) keyHolder.getKey()).get(0);
    }

    @Override
    public Director updateDirector(Director director) {
        int directorId = director.getId();

        jdbcTemplate.update("update directors set director_name = ? where director_id = ?;",
                director.getName(), directorId);

        return getDirectorById(directorId).get(0);
    }

    @Override
    public void deleteDirector(int id) {
        jdbcTemplate.update("delete from public.directors where director_id = " + id);
    }


    private RowMapper<Director> directorRowMapper() {
        return (rs, rowNum) -> new Director(rs.getInt("director_id"),
                rs.getString("director_name"));

    }

}

