package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        Map<String, String> params = Map.of(
                "name", film.getName(),
                "releaseDate", film.getReleaseDate().toString(),
                "description", film.getDescription(),
                "duration", film.getDuration().toString(),
                "mpa_id", film.getMpa().getId().toString());
        film.setId(simpleJdbcInsert.executeAndReturnKey(params).intValue());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "update films set name = ?, releaseDate = ?, description = ?, " +
                "duration = ?, mpa_id = ? where id = ?;";
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, film.getName());
            statement.setDate(2, Date.valueOf(film.getReleaseDate()));
            statement.setString(3, film.getDescription());
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.setInt(6, film.getId());
            return statement;
        });
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id where f.id = ?;";
        Film film = jdbcTemplate.queryForObject(sql, filmRowMapper(), id);
        jdbcTemplate.query("select count(user_id) from likes where film_id = ?;", (RowMapper<Film>) (rs, rowNum) -> {
            film.setLike(rs.getInt("count"));
            return null;
        }, film.getId());
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        Map<Integer, Film> filmMap = new HashMap<>();
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, m.name as mpa_name " +
                "from films f left outer join mpa m on f.mpa_id = m.id order by f.id;";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releasedate").toLocalDate(),
                    rs.getInt("duration"),
                    new Mpa(rs.getInt("mpa_id"),
                            rs.getString("mpa_name"))
            );
            film.setId(filmId);
            filmMap.put(filmId, film);
            return film;
        });
        jdbcTemplate.query("select * from film_genres fg left join genres g on fg.genre_id = g.id;",
                (RowMapper<Genre>) (rs, rowNum) -> {
                    do {
                        filmMap.get(rs.getInt("film_id")).getGenres().add(new Genre(
                                rs.getInt("genre_id"),
                                rs.getString("name")));
                    } while (rs.next());
                    return null;
                });
        jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id order by film_id;",
                (RowMapper<Film>) (rs, rowNum) -> {
                    do {
                        filmMap.get(rs.getInt("film_id")).setLike(rs.getInt("count"));
                    } while (rs.next());
                    return null;
                });
        return films;
    }

    @Override
    public void addLike(int id, int userId) {
        String sql = "insert into likes values (?, ?);";
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, id);
            return statement;
        });
    }

    @Override
    public void deleteLike(int id, int userId) {
        String sql = "delete from likes where user_id = ? and film_id = ?;";
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, id);
            return statement;
        });
    }

    @Override
    public void deleteFilm(Film film) {

    }

    private RowMapper<Film> filmsListRowMapper(Map<Integer, Film> filmMap) {
        return (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releasedate").toLocalDate(),
                    rs.getInt("duration"),
                    new Mpa(rs.getInt("mpa_id"),
                            rs.getString("mpa_name"))
            );
            film.setId(filmId);
            filmMap.put(filmId, film);
            do {
                if (filmId == rs.getInt("id")) {
                    film.getGenres().add(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
                } else break;
            } while (rs.next());
            return film;
        };
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releasedate").toLocalDate(),
                    rs.getInt("duration"),
                    new Mpa(rs.getInt("mpa_id"),
                            rs.getString("mpa_name"))
            );
            film.setId(rs.getInt("id"));
            do {
                film.getGenres().add(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
            } while (rs.next());
            return film;
        };
    }
}
