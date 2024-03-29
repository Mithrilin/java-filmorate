package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
public class FilmDbStorage implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
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
        //добавить связи фильм - директор
        addDirectorsToFilm(film);
        return addGenresToFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql =
                "UPDATE films " +
                        "SET " +
                        "name = ?, " +
                        "releaseDate = ?, " +
                        "description = ?, " +
                        "duration = ?, " +
                        "mpa_id = ?  " +
                        "where id = ?;";
        int result = jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, film.getName());
            statement.setDate(2, Date.valueOf(film.getReleaseDate()));
            statement.setString(3, film.getDescription());
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());
            statement.setInt(6, film.getId());
            return statement;
        });
        // Проверка на наличие фильма в БД
        if (result == 0) {
            return null;
        }
        //добавить связи фильм - директор
        addDirectorsToFilm(film);
        return addGenresToFilm(film);
    }

    @Override
    public List<Film> getFilmById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id = ?";
        return getFilmsList(sql, id);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql);
    }

    @Override
    public void addMark(int filmId, int userId, int mark) {
        String sql = "INSERT INTO marks (user_id, film_id, mark) VALUES (?, ?, ?);";
        jdbcTemplate.update(sql, userId, filmId, mark);
    }

    @Override
    public Integer deleteMark(int filmId, int userId) {
        String sql = "DELETE FROM marks WHERE user_id = ? AND film_id = ?;";
        return jdbcTemplate.update(sql, userId, filmId);
    }

    @Override
    public Integer deleteFilm(int id) {
        return jdbcTemplate.update("DELETE FROM films WHERE id = ?;", id);
    }

    @Override
    public List<Film> getPopularFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql);
    }

    @Override
    public List<Film> getPopularFilmsWithLimit(Integer count) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "LIMIT ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, count);
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer year) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE EXTRACT(YEAR FROM f.releasedate) = ? " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, year);
    }

    @Override
    public List<Film> getPopularFilmsByYearWithLimit(Integer count, Integer year) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE EXTRACT(YEAR FROM f2.releasedate) = ? " +
                                "LIMIT ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, year, count);
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Integer genreId) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN film_genres AS fg2 ON f2.id = fg2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE fg2.genre_id = ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, genreId);
    }

    @Override
    public List<Film> getPopularFilmsByGenreWithLimit(Integer count, Integer genreId) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN film_genres AS fg2 ON f2.id = fg2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE fg2.genre_id = ? " +
                                "LIMIT ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, genreId, count);
    }

    @Override
    public List<Film> getPopularFilmsByYearAndGenre(Integer genreId, Integer year) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN film_genres AS fg2 ON f2.id = fg2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE EXTRACT(YEAR FROM f2.releasedate) = ? " +
                                "AND fg2.genre_id = ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, year, genreId);
    }

    @Override
    public List<Film> getPopularFilmsByYearAndGenreWithLimit(Integer count, Integer genreId, Integer year) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN film_genres AS fg2 ON f2.id = fg2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE EXTRACT(YEAR FROM f2.releasedate) = ? " +
                                "AND fg2.genre_id = ? " +
                                "LIMIT ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, year, genreId, count);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT mk2.film_id " +
                                "FROM marks AS mk2 " +
                                "WHERE mk2.user_id = ? " +
                                "AND mk2.mark > 5 " +
                                "AND mk2.film_id IN (SELECT film_id " +
                                                "FROM marks " +
                                                "WHERE user_id = ? " +
                                                "AND mark > 5)) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, userId, friendId);
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortByYear(int directorId) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN directors_film AS df2 ON f2.id = df2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE df2.director_id = ?) " +
                "ORDER BY f.releasedate";
        return getFilmsList(sql, directorId);
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortByLikes(int directorId) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (SELECT f2.id " +
                                "FROM films AS f2 " +
                                "LEFT JOIN directors_film AS df2 ON f2.id = df2.film_id " +
                                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                                            "FROM marks " +
                                            "GROUP BY film_id " +
                                            "ORDER BY rating_count DESC) AS mk ON f2.id = mk.film_id " +
                                "WHERE df2.director_id = ?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        return getFilmsList(sql, directorId);
    }

    @Override
    public List<Film> getFilmsByTitleSearch(String query) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE UPPER (f.name) LIKE UPPER (?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        String param = "%" + query + "%";
        return getFilmsList(sql, param);
    }

    @Override
    public List<Film> getFilmsByTitleAndDirectorSearch(String query) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE UPPER (f.name) LIKE UPPER (?) " +
                "OR UPPER (d.director_name) LIKE UPPER (?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        String param = "%" + query + "%";
        return getFilmsList(sql, param, param);
    }

    @Override
    public List<Film> getFilmsByDirectorSearch(String query) {
        String sql = "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, df.director_id, d.director_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE UPPER (d.director_name) LIKE UPPER (?) " +
                "ORDER BY mk.rating_count DESC NULLS LAST";
        String param = "%" + query + "%";
        return getFilmsList(sql, param);
    }

    @Override
    public List<Film> getRecommendations(List<Integer> filmIdsForRecommendation) {
        String sql =
                "SELECT f.*, m.name AS mpa_name, mk.rating_count " +
                        "FROM films AS f " +
                        "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                        "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                        "FROM marks " +
                        "GROUP BY film_id " +
                        "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                        "WHERE f.id IN (:filmIdsForRecommendation) " +
                        "ORDER BY mk.rating_count DESC NULLS LAST";
        SqlParameterSource parameters = new MapSqlParameterSource("filmIdsForRecommendation", filmIdsForRecommendation);
        return namedJdbcTemplate.query(sql, parameters, filmsRowMapper());
    }

    @Override
    public Map<Integer, List<Genre>> getFilmIdToGenres(List<Integer> filmIdsForRecommendation) {
        String sql =
                "SELECT fg.film_id, fg.genre_id, g.name " +
                        "FROM film_genres AS fg " +
                        "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                        "WHERE fg.film_id IN (:filmIdsForRecommendation) " +
                        "ORDER BY fg.film_id";
        SqlParameterSource parameters = new MapSqlParameterSource("filmIdsForRecommendation", filmIdsForRecommendation);
        List<Map<Integer, Genre>> filmIdToGenreList = namedJdbcTemplate.query(sql, parameters, genresRowMapper());
        return filmIdToGenreList.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    @Override
    public Map<Integer, List<Director>> getFilmIdToDirectors(List<Integer> filmIdsForRecommendation) {
        String sql =
                "SELECT df.film_id, df.director_id, d.director_name " +
                        "FROM directors_film AS df " +
                        "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                        "WHERE df.film_id IN (:filmIdsForRecommendation) " +
                        "ORDER BY df.film_id";
        SqlParameterSource parameters = new MapSqlParameterSource("filmIdsForRecommendation", filmIdsForRecommendation);
        List<Map<Integer, Director>> filmIdToDirectorList = namedJdbcTemplate.query(sql, parameters, directorsRowMapper());
        return filmIdToDirectorList.stream()
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    private RowMapper<Film> filmsRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("releasedate").toLocalDate(),
                    rs.getInt("duration"),
                    new Mpa(rs.getInt("mpa_id"),
                            rs.getString("mpa_name")));
            film.setRating(rs.getDouble("rating_count"));
            film.setId(rs.getInt("id"));
            return film;
        };
    }

    private RowMapper<Map<Integer, Genre>> genresRowMapper() {
        return (rs, rowNum) -> {
            Map<Integer, Genre> filmIdToGenre = new HashMap<>();
            Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
            filmIdToGenre.put(rs.getInt("film_id"), genre);
            return filmIdToGenre;
        };
    }

    private RowMapper<Map<Integer, Director>> directorsRowMapper() {
        return (rs, rowNum) -> {
            Map<Integer, Director> filmIdToDirector = new HashMap<>();
            Director director = new Director(rs.getInt("director_id"), rs.getString("director_name"));
            filmIdToDirector.put(rs.getInt("film_id"), director);
            return filmIdToDirector;
        };
    }

    private Film getNewFilm(ResultSet rs) throws SQLException {
        Film film = new Film(
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releasedate").toLocalDate(),
                rs.getInt("duration"),
                new Mpa(rs.getInt("mpa_id"),
                        rs.getString("mpa_name")));
        film.setRating(rs.getDouble("rating_count"));
        return film;
    }

    private RowMapper<Film> filmListRowMapper(List<Film> films) {
        Map<Integer, Film> filmMap = new HashMap<>();
        Map<Integer, HashMap<Integer, Genre>> filmGenreMap = new HashMap<>();
        Map<Integer, HashMap<Integer, Director>> filmDirectorMap = new HashMap<>();
        return (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            if (!filmMap.containsKey(filmId)) {
                Film film = FilmDbStorage.this.getNewFilm(rs);
                film.setId(filmId);
                filmMap.put(filmId, film);
                films.add(film);
                filmGenreMap.put(filmId, new HashMap<>());
                filmDirectorMap.put(filmId, new HashMap<>());
            }
            int genreId = rs.getInt("genre_id");
            if (genreId != 0 && !filmGenreMap.get(filmId).containsKey(genreId)) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                filmGenreMap.get(filmId).put(genreId, genre);
                filmMap.get(filmId).getGenres().add(genre);
            }
            int directorId = rs.getInt("director_id");
            if (directorId != 0 && !filmDirectorMap.get(filmId).containsKey(directorId)) {
                Director director = new Director(directorId, rs.getString("director_name"));
                filmDirectorMap.get(filmId).put(directorId, director);
                filmMap.get(filmId).getDirectors().add(director);
            }
            return filmMap.get(filmId);
        };
    }

    // Метод добавления жанров в фильмы
    private Film addGenresToFilm(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?;", film.getId());
        // Проверка, что у фильма есть жанры
        if (film.getGenres().isEmpty()) {
            return film;
        }
        // Проверка на дубликаты жанров во входящем фильме
        Map<Integer, Genre> genresMap = new HashMap<>();
        List<Genre> genres = new ArrayList<>();
        for (Genre genre : film.getGenres()) {
            if (!genresMap.containsKey(genre.getId())) {
                genresMap.put(genre.getId(), genre);
                genres.add(genre);
            }
        }
        film.setGenres(genres);
        String sql = "INSERT INTO film_genres VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, genres.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genres.size();
            }
        });
        return film;
    }

    private void addDirectorsToFilm(Film film) {
        int filmId = film.getId();
        jdbcTemplate.update("delete from directors_film where film_id = ?;", filmId);
        // Проверка, что у фильма есть режиссеры
        if (film.getDirectors().isEmpty()) {
            return;
        }
        // Проверка на дубликаты жанров во входящем фильме
        Map<Integer, Director> directorsMap = new HashMap<>();
        List<Director> directors = new ArrayList<>();
        for (Director director : film.getDirectors()) {
            if (!directorsMap.containsKey(director.getId())) {
                directorsMap.put(director.getId(), director);
                directors.add(director);
            }
        }
        film.setDirectors(directors);
        String sql = "INSERT INTO directors_film VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, directors.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directors.size();
            }
        });
    }

    private List<Film> getFilmsList(String sql, @Nullable Object... args) {
        List<Film> films = new ArrayList<>();
        jdbcTemplate.query(sql, filmListRowMapper(films), args);
        return films;
    }
}