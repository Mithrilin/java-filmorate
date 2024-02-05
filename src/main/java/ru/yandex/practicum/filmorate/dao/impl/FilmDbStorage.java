package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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

        //добавить связи фильм - директор
        addDirectorsFromFilm(film);

        return addGenresFromFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "update films set name = ?, releaseDate = ?, description = ?, " +
                "duration = ?, mpa_id = ?  where id = ?;";
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

        //добавить связи фильм - директор
        addDirectorsFromFilm(film);

        return addGenresFromFilm(film);
    }

    @Override
    public List<Film> getFilmById(int id) {
        String sql = "select f.id, f.name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, fg.genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id " +
                "where f.id = ?;";
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
                "m.name as mpa_name, " +
                "g.id as genre_id, g.name as genre_name " +
                "from films f " +
                "left outer join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id " +
                "order by f.id;";
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
        String sql = "merge into likes as l1 " +
                "using (values(?, ?)) as l2 (user_id, film_id) " +
                "ON l1.user_id=l2.user_id and l1.FILM_ID=l2.film_id " +
                "when matched then update set l1.USER_ID=l2.user_id and l1.FILM_ID=l2.film_id " +
                "when not matched then insert (user_id, film_id) values(l2.user_id, l2.film_id);";
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

    @Override
    public List<Film> getPopularFilms(Integer count) {
        List<Film> films;
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        if (count != null) {
            sql = sql + " LIMIT " + count;
        }
        films = jdbcTemplate.query(sql, this::filmRowWithLikes);
        return insertGenresAndDirectorsIntoFilms(films);
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer count, Integer year) {
        List<Film> films;
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "WHERE YEAR(f.releasedate) = ? " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        if (count != null) {
            sql = sql + " LIMIT " + count;
        }
        films = jdbcTemplate.query(sql, this::filmRowWithLikes, year);
        return insertGenresAndDirectorsIntoFilms(films);
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        List<Film> films;
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        if (count != null) {
            sql = sql + " LIMIT " + count;
        }
        films = jdbcTemplate.query(sql, this::filmRowWithLikes, genreId);
        return insertGenresAndDirectorsIntoFilms(films);
    }

    @Override
    public List<Film> getPopularFilmsByYearAndGenre(Integer count, Integer genreId, Integer year) {
        List<Film> films;
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ? AND YEAR(f.releasedate) = ? " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        if (count != null) {
            sql = sql + " LIMIT " + count;
        }
        films = jdbcTemplate.query(sql, this::filmRowWithLikes, genreId, year);
        return insertGenresAndDirectorsIntoFilms(films);
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
        return insertGenresAndDirectorsIntoFilms(commonFilms);
    }


    @Override
    public List<Film> getFilmsSortYearByDirectorId(int directorId) {
        String sqlSortYear = "select df.film_id " +
                "from directors_film df " +
                "join films f on df.film_id = f.id " +
                "where df.director_id = ? " +
                "order by extract(year from cast(f.releasedate as date));";

        List<Integer> filmsId = jdbcTemplate.queryForList(sqlSortYear, Integer.class, directorId);

        return filmsId.stream().map(fId -> getFilmById(fId).get(0)).collect(Collectors.toList());
    }

    @Override
    public List<Film> getFilmsSortLikesByDirectorId(int directorId) {
        //проверка на наличие режиссера
        if (jdbcTemplate.queryForList(
                "select director_id from directors where director_id = ?;", Integer.class, directorId).isEmpty()
        ) {
            throw new NotFoundException(String.format("Режиссер под id = %s не найден", directorId));
        }

        String sqlSortLikes = "select df.film_id " +
                "from directors_film df " +
                "left join likes l on df.film_id = l.film_id " +
                "where df.director_id = ? " +
                "group by df.film_id " +
                "order by count(l.user_id);";

        List<Integer> filmsId = jdbcTemplate.queryForList(sqlSortLikes, Integer.class, directorId);

        return filmsId.stream().map(fId -> getFilmById(fId).get(0)).collect(Collectors.toList());
    }

    @Override
    public List<Film> getFilmsByTitleSearch(String query) {
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "WHERE UPPER (f.name) LIKE UPPER (?) " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        List<Film> movies = jdbcTemplate.query(sql, this::filmRowWithLikes, "%" + query + "%");
        if (movies.isEmpty()) return new ArrayList<>();
        return insertGenresAndDirectorsIntoFilms(movies);
    }

    @Override
    public List<Film> getFilmsByTitleAndDirectorSearch(String query) {
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN directors_film AS df ON df.film_id = f.id " +
                "LEFT JOIN directors AS d ON d.director_id = df.director_id " +
                "WHERE UPPER (f.name) LIKE UPPER (?) OR UPPER (d.director_name) LIKE UPPER (?) " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        String param = "%" + query + "%";
        List<Film> movies = jdbcTemplate.query(sql, this::filmRowWithLikes, param, param);
        if (movies.isEmpty()) return new ArrayList<>();
        return insertGenresAndDirectorsIntoFilms(movies);
    }

    @Override
    public List<Film> getFilmsByDirectorSearch(String query) {
        String sql = "SELECT f.*, m.id AS mpaId, m.name AS mpaName, COUNT (l.user_id) AS film_likes " +
                "FROM films AS f " +
                "JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN likes AS l ON l.film_id = f.id " +
                "LEFT JOIN directors_film AS df ON df.film_id = f.id " +
                "LEFT JOIN directors AS d ON d.director_id = df.director_id " +
                "WHERE UPPER (d.director_name) LIKE UPPER (?) " +
                "GROUP BY f.id " +
                "ORDER BY film_likes DESC";
        List<Film> movies = jdbcTemplate.query(sql, this::filmRowWithLikes, "%" + query + "%");
        if (movies.isEmpty()) return new ArrayList<>();
        return insertGenresAndDirectorsIntoFilms(movies);
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

            //добавить режиссеров в фильм
            film.setDirectors(getDirectorsFilm(film.getId()));

            return film;
        };
    }

    //для добавления режиссеров к объекту фильма
    private Set<Director> getDirectorsFilm(int filmId) {

        Set<Director> setDirectors = new HashSet<>(1);

        SqlRowSet directors = jdbcTemplate.queryForRowSet(
                "select d.director_id, d.director_name " +
                        "from public.directors_film df " +
                        "join directors d on df.director_id = d.director_id " +
                        "where df.film_id = " + filmId + ";"
        );

        while (directors.next()) {
            Director director = new Director(
                    directors.getInt("director_id"),
                    directors.getString("director_name")
            );
            setDirectors.add(director);
        }

        return setDirectors;
    }

    private Film getNewFilm(ResultSet rs) throws SQLException {
        return new Film(
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releasedate").toLocalDate(),
                rs.getInt("duration"),
                new Mpa(rs.getInt("mpa_id"),
                        rs.getString("mpa_name"))
        );
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
            //добавить режиссеров в фильм
            filmMap.get(filmId).setDirectors(getDirectorsFilm(filmId));
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

    private void addDirectorsFromFilm(Film film) {
        int filmId = film.getId();

        // Проверка, что у фильма есть режиссеры
        if (film.getDirectors().isEmpty()) {
            //удалить все связи фильм - режиссер если они были
            jdbcTemplate.update("delete from directors_film where film_id = " + filmId + ";");
            return;
        }

        //проверить что режиссер такой есть в базе возвращает сет из id режиссеров
        String values = isDirector(film).stream()
                .map(dId -> String.format("(%s, %s)", filmId, dId))
                .collect(Collectors.joining(","));

        //удалить все связи фильм - режиссер
        jdbcTemplate.update("delete from directors_film where film_id = " + filmId + ";");

        //добавить связь межу режиссерами и фильмом
        jdbcTemplate.update("insert into directors_film (film_id, director_id) values " + values + ";");

    }

    //проверка есть ли режиссеры в базе
    private Set<Integer> isDirector(Film film) {

        List<Integer> directorsId = jdbcTemplate.queryForList("select director_id from directors", Integer.class);
        return film.getDirectors().stream()
                .map(Director::getId)
                .map(dId -> {
                    if (!directorsId.contains(dId)) {
                        throw new NotFoundException("Не найден режиссер под id = " + dId);
                    }
                    return dId;
                }).collect(Collectors.toSet());
    }

    private List<Film> insertGenresAndDirectorsIntoFilms(List<Film> films) {
        List<Integer> filmsId = films.stream().map(Film::getId).collect(Collectors.toList());
        String inSql = String.join(",", Collections.nCopies(filmsId.size(), "?"));
        String sqlGenres = String.format("SELECT fg.film_id AS filmId, g.id AS genreId, g.name AS genreName " +
                "FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (%s)", inSql);
        String sqlDirectors = String.format("SELECT df.film_id AS filmId, d.director_id AS directorId, " +
                "d.director_name AS directorName " +
                "FROM directors_film AS df " +
                "JOIN directors AS d ON df.director_id = d.director_id " +
                "WHERE df.film_id IN (%s)", inSql);
        SqlRowSet sqlRowSetGenres = jdbcTemplate.queryForRowSet(sqlGenres, filmsId.toArray());
        SqlRowSet sqlRowSetDirectors = jdbcTemplate.queryForRowSet(sqlDirectors, filmsId.toArray());
        Map<Integer, List<Genre>> genresMap = new HashMap<>();
        Map<Integer, Set<Director>> directorsMap = new HashMap<>();

        while (sqlRowSetGenres.next()) {
            int filmId = sqlRowSetGenres.getInt("FILMID");
            int genreId = sqlRowSetGenres.getInt("GENREID");
            String genreName = sqlRowSetGenres.getString("GENRENAME");
            Genre genre = new Genre(genreId, genreName);
            if (!genresMap.containsKey(filmId)) {
                genresMap.put(filmId, new ArrayList<>());
            }
            genresMap.get(filmId).add(genre);
        }
        while (sqlRowSetDirectors.next()) {
            int filmId = sqlRowSetDirectors.getInt("FILMID");
            int directorId = sqlRowSetDirectors.getInt("DIRECTORID");
            String directorName = sqlRowSetDirectors.getString("DIRECTORNAME");
            Director director = new Director(directorId, directorName);
            if (!directorsMap.containsKey(filmId)) {
                directorsMap.put(filmId, new HashSet<>());
            }
            directorsMap.get(filmId).add(director);
        }

        for (Film film : films) {
            if (genresMap.containsKey(film.getId())) {
                film.setGenres(genresMap.get(film.getId()));
            }
            if (directorsMap.containsKey(film.getId())) {
                film.setDirectors(directorsMap.get(film.getId()));
            }
        }
        return films;
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
