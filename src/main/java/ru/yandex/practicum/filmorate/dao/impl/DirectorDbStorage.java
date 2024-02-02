package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;


import java.sql.PreparedStatement;
import java.util.List;

@Component("directorDbStorage")
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
    public Director getDirectorById(int id) {

        List<Director> listDirector = jdbcTemplate.query(
                "select * from directors where director_id = ?;", directorRowMapper(), id
        );

        if (listDirector.isEmpty()) {
            throw new NotFoundException("Не найден режиссер под id = " + id);
        }

        return listDirector.get(0);
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


        return getDirectorById((int) keyHolder.getKey());
    }

    @Override
    public Director updateDirector(Director director) {
        int directorId = director.getId();

        jdbcTemplate.update("update directors set director_name = ? where director_id = ?;",
                director.getName(), directorId);

        return getDirectorById(directorId);
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

