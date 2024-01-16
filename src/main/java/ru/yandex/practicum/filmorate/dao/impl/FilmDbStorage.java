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
import java.util.*;

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
        }, id);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        Map<Integer, Film> filmMap = new HashMap<>();
        Map<Integer, List<Genre>> genreMap = new HashMap<>();
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id order by f.id;";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            Genre genre;
            if (!filmMap.containsKey(filmId)) {
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
            }
            if (!genreMap.containsKey(filmId)) {
                List<Genre> genreList = new ArrayList<>();
                genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name"));
                genreList.add(genre);
                genreMap.put(filmId, genreList);
            } else {
                genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name"));
                genreMap.get(filmId).add(genre);
            }
            filmMap.get(filmId).getGenres().add(genre);
            return null;
        });
        jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id order by film_id;",
                (RowMapper<Film>) (rs, rowNum) -> {
                    do {
                        filmMap.get(rs.getInt("film_id")).setLike(rs.getInt("count"));
                    } while (rs.next());
                    return null;
                });
        return new ArrayList<>(filmMap.values());
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
    public List<Film> getPopularFilms(String count) {
        Map<Integer, Integer> liksMap = new HashMap<>();
        Map<Integer, Film> filmMap = new HashMap<>();
        Map<Integer, List<Genre>> genreMap = new HashMap<>();
        if (count != null) {
            int length = Integer.parseInt(count);
            jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id " +
                            "order by count(user_id) desc limit ?;",
                    likeRowMapper(liksMap), length);
        } else {
            jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id " +
                            "order by count(user_id) desc;",
                    likeRowMapper(liksMap));
        }
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id order by f.id;";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            if (!liksMap.containsKey(filmId)) {
                return null;
            }
            Genre genre;
            if (!filmMap.containsKey(filmId)) {
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
            }
            if (!genreMap.containsKey(filmId)) {
                List<Genre> genreList = new ArrayList<>();
                genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name"));
                genreList.add(genre);
                genreMap.put(filmId, genreList);
            } else {
                genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name"));
                genreMap.get(filmId).add(genre);
            }
            filmMap.get(filmId).getGenres().add(genre);
            return null;
        });
        filmMap.values().stream().

        return new ArrayList<>(filmMap.values());
    }

    @Override
    public void deleteFilm(Film film) {

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

    private RowMapper<Film> likeRowMapper(Map<Integer, Integer> liksMap) {
        return (rs, rowNum) -> {
            do {
                liksMap.put(rs.getInt("film_id"), rs.getInt("count"));
            } while (rs.next());
            return null;
        };
    }
}
