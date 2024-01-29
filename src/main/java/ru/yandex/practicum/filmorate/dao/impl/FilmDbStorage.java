package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.dao.FilmDao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    /** Метод получения популярных фильмов.
     * Если значение count задано, то такое количество фильмов и попадёт в список.
     * Если фильмов с лайками меньше чем значение count, то остаток заполнится фильмами без лайков.
     * Если значение count не задано, то в список попадут все фильмы.
     **/
    @Override
    public List<Film> getPopularFilms(String count) {
        Map<Integer, Integer> liksMap = new HashMap<>();
        List<Film> films = new ArrayList<>();
        List<Integer> priority;
        int length = 0;
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id order by f.id;";
        // Сборка всех фильмов в мапу
        Map<Integer, Film> filmMap = getFilmMap(sql);
        // Проверка наличия значения count (размера списка)
        if (count == null) {
            priority = jdbcTemplate.query("select film_id, count(user_id) from likes " +
                            "group by film_id order by count(user_id) desc;",
                    likeRowMapper(liksMap));
        } else {
            length = Integer.parseInt(count);
            priority = jdbcTemplate.query("select film_id, count(user_id) from likes " +
                            "group by film_id order by count(user_id) desc limit ?;",
                    likeRowMapper(liksMap), length);
        }
        // Проверка наличия лайков
        if (!priority.isEmpty()) {
            for (Integer i : priority) {
                Film film = filmMap.get(i);
                film.setLike(liksMap.get(i));
                films.add(film);
            }
        }
        // Формирование итогового списка с учётом заданного размера
        for (Film film : filmMap.values()) {
            if (count != null && films.size() >= length) break;
            if (!liksMap.containsKey(film.getId())) {
                films.add(film);
            }
        }
        return films;
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
}
