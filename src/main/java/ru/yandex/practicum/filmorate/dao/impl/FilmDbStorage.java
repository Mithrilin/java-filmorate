package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmDao {
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
        return addGenresFromFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "update films set name = ?, releaseDate = ?, description = ?, " +
                "duration = ?, mpa_id = ? where id = ?;";
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
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден.");
        }
        jdbcTemplate.update("delete from film_genres where film_id = ?;", film.getId());
        return addGenresFromFilm(film);
    }

    @Override
    public List<Film> getFilmById(int id) {
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, fg.genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id where f.id = ?;";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), id);
        // Проверка на наличие фильма в БД
        if (films.size() == 0) {
            return films;
        }
        // Добавление в фильм количество лайков
        jdbcTemplate.query("select count(user_id) from likes where film_id = ?;", (RowMapper<Film>) (rs, rowNum) -> {
            films.get(0).setLike(rs.getInt("count(user_id)"));
            return null;
        }, id);
        return films;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id order by f.id;";
        // Сборка всех фильмов в мапу
        Map<Integer, Film> filmMap = getFilmMap(sql);
        // Добавление в фильм количество лайков
        jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id order by film_id;",
                (RowMapper<Film>) (rs, rowNum) -> {
                    do {
                        filmMap.get(rs.getInt("film_id")).setLike(rs.getInt("count(user_id)"));
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
    public Integer deleteLike(int id, int userId) {
        String sql = "delete from likes where user_id = ? and film_id = ?;";
        return jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, id);
            return statement;
        });
    }

    @Override
    public Integer deleteFilm(int id) {
        return jdbcTemplate.update("delete from films where id = ?;", id);
    }

    /**
     * Метод получения популярных фильмов.
     * Если значение count задано, то такое количество фильмов и попадёт в список.
     * Если фильмов с лайками меньше чем значение count, то остаток заполнится фильмами без лайков.
     * Если значение count не задано, то в список попадут все фильмы.
     **/
    @Override
    public List<Film> getPopularFilms(String count, String genreId, String year) {
        List<Film> films;
        int length = 0;
        int gId = 0;
        int y = 0;

        if (genreId != null) {
            gId = Integer.parseInt(genreId);
        }
        if (year != null) {
            y = Integer.parseInt(year);
        }
// Делаем запрос всех популярных фильмов по году и/или жанру
        if (count == null) {
            if (gId == 0) {
                if (y == 0) {
                    films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                            "FROM films AS f " +
                            "JOIN mpa AS m ON f.mpa_id = m.id " +
                            "LEFT JOIN likes AS l ON l.film_id = f.id " +
                            "GROUP BY f.id " +
                            "ORDER BY film_likes DESC", this::filmRowWithLikes);
                } else {
                    films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                            "FROM films AS f " +
                            "JOIN mpa AS m ON f.mpa_id = m.id " +
                            "LEFT JOIN likes AS l ON l.film_id = f.id " +
                            "WHERE YEAR(f.releasedate) = ? " +
                            "GROUP BY f.id " +
                            "ORDER BY film_likes DESC", this::filmRowWithLikes, y);
                }
            } else if (y == 0) {
                films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                        "FROM films AS f " +
                        "JOIN mpa AS m ON f.mpa_id = m.id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? " +
                        "GROUP BY f.id " +
                        "ORDER BY film_likes DESC", this::filmRowWithLikes, gId);
            } else {
                films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                        "FROM films AS f " +
                        "JOIN mpa AS m ON f.mpa_id = m.id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? AND YEAR(f.releasedate) = ? " +
                        "GROUP BY f.id " +
                        "ORDER BY film_likes DESC", this::filmRowWithLikes, gId, y);
            }
            // Делаем запрос популярных фильмов по году и/или жанру с ограничением по количеству
        } else {
            length = Integer.parseInt(count);
            if (gId == 0) {
                if (y == 0) {
                    films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                            "FROM films AS f " +
                            "JOIN mpa AS m ON f.mpa_id = m.id " +
                            "LEFT JOIN likes AS l ON l.film_id = f.id " +
                            "GROUP BY f.id " +
                            "ORDER BY film_likes DESC " +
                            "LIMIT ?", this::filmRowWithLikes, length);
                } else {
                    films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                            "FROM films AS f " +
                            "JOIN mpa AS m ON f.mpa_id = m.id " +
                            "LEFT JOIN likes AS l ON l.film_id = f.id " +
                            "WHERE YEAR(f.releasedate) = ? " +
                            "GROUP BY f.id " +
                            "ORDER BY film_likes DESC " +
                            "LIMIT ?", this::filmRowWithLikes, y, length);
                }
            } else if (y == 0) {
                films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                        "FROM films AS f " +
                        "JOIN mpa AS m ON f.mpa_id = m.id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? " +
                        "GROUP BY f.id " +
                        "ORDER BY film_likes DESC " +
                        "LIMIT ?", this::filmRowWithLikes, gId, length);
            } else {
                films = jdbcTemplate.query("SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                        "FROM films AS f " +
                        "JOIN mpa AS m ON f.mpa_id = m.id " +
                        "LEFT JOIN likes AS l ON l.film_id = f.id " +
                        "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? AND YEAR(f.releasedate) = ? " +
                        "GROUP BY f.id " +
                        "ORDER BY film_likes DESC " +
                        "LIMIT ?", this::filmRowWithLikes, gId, y, length);
            }
        }
// Добавляем жанры к выбранным фильмам
        for (Film film : films) {
            Map<Integer, Genre> genresMap = new HashMap<>();
            for (Genre genre : film.getGenres()) {
                if (!genresMap.containsKey(genre.getId())) {
                    genresMap.put(genre.getId(), genre);
                }
            }
        }

        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "JOIN likes AS l ON l.film_id = f.id " +
                "WHERE f.id IN (" +
                "SELECT l.film_id FROM likes AS l " +
                "WHERE l.user_id = ? AND l.film_id IN (" +
                "SELECT l.film_id FROM likes AS l " +
                "WHERE l.user_id = ?)) " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        List<Film> commonFilms = jdbcTemplate.query(sql, this::filmRowWithLikes, userId, friendId);
        if (commonFilms.isEmpty()) return new ArrayList<>();
        List<Integer> filmsId = commonFilms.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(filmsId.size(), "?"));
        String sqlGenres = String.format("SELECT fg.film_id AS filmId, g.id AS genreId, g.name AS genreName " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (%s)", inSql);
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sqlGenres, filmsId.toArray());
        Map<Integer, List<Genre>> genresMap = new HashMap<>();
        while (sqlRowSet.next()) {
            int filmId = sqlRowSet.getInt("FILMID");
            int genreId = sqlRowSet.getInt("GENREID");
            String genreName = sqlRowSet.getString("GENRENAME");
            Genre genre = new Genre(genreId, genreName);
            if (!genresMap.containsKey(filmId)) {
                genresMap.put(filmId, new ArrayList<>());
            }
            genresMap.get(filmId).add(genre);
        }
        for (Film film : commonFilms) {
            if (genresMap.containsKey(film.getId()))
                film.setGenres(genresMap.get(film.getId()));
        }
        return commonFilms;
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = getNewFilm(rs);
            film.setId(rs.getInt("id"));
            do {
                Genre genre = new Genre(
                        rs.getInt("genre_id"),
                        rs.getString("genre_name"));
                if (genre.getId() != 0) {
                    film.getGenres().add(genre);
                }
            } while (rs.next());
            return film;
        };
    }

    private RowMapper<Integer> likeRowMapper(Map<Integer, Integer> liksMap) {
        return (rs, rowNum) -> {
            liksMap.put(rs.getInt("film_id"), rs.getInt("count(user_id)"));
            return rs.getInt("film_id");
        };
    }

    private Film getNewFilm(ResultSet rs) throws SQLException {
        return new Film(
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releasedate").toLocalDate(),
                rs.getInt("duration"),
                new Mpa(rs.getInt("mpa_id"),
                        rs.getString("mpa_name")));
    }

    // Метод сборки всех фильмов в мапу с записью жанров
    private Map<Integer, Film> getFilmMap(String sql) {
        Map<Integer, Film> filmMap = new HashMap<>();
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            if (!filmMap.containsKey(filmId)) {
                Film film = getNewFilm(rs);
                film.setId(filmId);
                filmMap.put(filmId, film);
            }
            Genre genre = new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("genre_name"));
            if (genre.getId() != 0) {
                filmMap.get(filmId).getGenres().add(genre);
            }
            return null;
        });
        return filmMap;
    }

    // Метод добавления жанров в фильмы
    private Film addGenresFromFilm(Film film) {
        // Проверка, что у фильма есть жанры
        if (film.getGenres().isEmpty()) {
            return film;
        }
        // Проверка на дубликаты жанров
        Map<Integer, Genre> genresMap = new HashMap<>();
        for (Genre genre : film.getGenres()) {
            if (!genresMap.containsKey(genre.getId())) {
                genresMap.put(genre.getId(), genre);
            }
        }
        film.setGenres(new ArrayList<>(genresMap.values()));
        StringBuilder sql = new StringBuilder("insert into film_genres values");
        sql.append(" (?, ?),".repeat(film.getGenres().size()));
        sql.deleteCharAt(sql.lastIndexOf(",")).append(";");
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql.toString());
            int questionMarkNumber = 0;
            for (int i = 0; i < film.getGenres().size(); i++) {
                statement.setInt(questionMarkNumber + 1, film.getId());
                statement.setInt(questionMarkNumber + 2, film.getGenres().get(i).getId());
                questionMarkNumber += 2;
            }
            return statement;
        });
        return film;
    }

    private Film filmRowWithLikes(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film(rs.getString("name"), rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(), rs.getInt("duration"),
                new Mpa(rs.getInt("mpaId"), rs.getString("mpaName")));
        film.setId((rs.getInt("id")));
        film.setLike(rs.getInt(("film_likes")));
        return film;
    }
}
