package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.dto.params.MarksParams;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("userDbStorage")
public class UserDbStorage implements UserDao {
    private static final String USERS_MARKS_SQL =
            "SELECT * " +
            "FROM marks AS m1 " +
            "WHERE m1.user_id IN (SELECT m2.user_id " +
                                  "FROM marks AS m2 " +
                                  "WHERE m2.film_id IN (SELECT m3.film_id " +
                                                        "FROM marks AS m3 " +
                                                        "WHERE m3.user_id = ?))";
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
        Map<String, String> params = Map.of(
                "login", user.getLogin(),
                "name", user.getName(),
                "email", user.getEmail(),
                "birthday", user.getBirthday().toString());
        user.setId(simpleJdbcInsert.executeAndReturnKey(params).intValue());
        return user;
    }

    @Override
    public Integer updateUser(User user) {
        String sql = "update users set login = ?, name = ?, email = ?, birthday = ? where id = ?;";
        return jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            statement.setInt(5, user.getId());
            return statement;
        });
    }

    @Override
    public List<User> getUserById(int id) {
        String sql = "select * from users u left outer join friends f on u.id = f.user_id where u.id = ?;";
        return jdbcTemplate.query(sql, userRowMapper(), id);
    }

    @Override
    public List<User> getAllUsers() {
        Map<Integer, User> usersMap = new HashMap<>();
        String sql = "select * from users;";
        List<User> users = jdbcTemplate.query(sql, usersListRowMapper(usersMap));
        // Добавление ид друзей в список пользователю
        jdbcTemplate.query("select * from friends;", (RowMapper<Integer>) (rs, rowNum) -> {
            do {
                usersMap.get(rs.getInt("user_id")).getFriends().add(rs.getInt("friend_id"));
            } while (rs.next());
            return null;
        });
        return users;
    }

    @Override
    public Integer deleteUser(int id) {
        return jdbcTemplate.update("delete from users where id = ?;", id);
    }

    @Override
    public Integer addFriend(int id, int friendId) {
        return jdbcTemplate.update("insert into friends (user_id, friend_id) values (?, ?);", id, friendId);
    }

    @Override
    public Integer deleteFriend(int id, int friendId) {
        return jdbcTemplate.update("delete from friends where user_id = ? and friend_id = ?;", id, friendId);
    }

    @Override
    public List<User> getAllFriends(int id) {
        Map<Integer, User> usersMap = new HashMap<>();
        String sql = "select * from friends f join users u on f.friend_id = u.id where f.user_id = ?;";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = getNewUser(rs);
            user.setId(rs.getInt("id"));
            usersMap.put(rs.getInt("id"), user);
            return user;
        }, id);
        // Добавление ид друзей в список пользователю
        jdbcTemplate.query("select * from friends;", (RowMapper<Integer>) (rs, rowNum) -> {
            do {
                if (usersMap.containsKey(rs.getInt("user_id"))) {
                    usersMap.get(rs.getInt("user_id")).getFriends().add(rs.getInt("friend_id"));
                }
            } while (rs.next());
            return null;
        });
        return users;
    }

    @Override
    public List<User> getAllCommonFriends(int id, int otherId) {
        Map<Integer, User> usersMap = new HashMap<>();
        List<User> users = new ArrayList<>();
        String sql = "select * from friends f join users u on f.friend_id = u.id where f.user_id in (?, ?);";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            int userId = rs.getInt("id");
            if (usersMap.containsKey(userId)) {
                users.add(usersMap.get(userId));
            } else {
                User user = getNewUser(rs);
                user.setId(userId);
                usersMap.put(userId, user);
            }
            return null;
        }, id, otherId);
        return users;
    }

    @Override
    public Map<Integer, HashMap<Integer, Integer>> getUserIdToFilmIdWithMark(int requesterId) {
        List<MarksParams> marksParamsList = jdbcTemplate.query(USERS_MARKS_SQL, marksRowMapper(), requesterId);
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark = new HashMap<>();
        for (MarksParams marksParams : marksParamsList) {
            int userId = marksParams.getUserId();
            int filmId = marksParams.getFilmId();
            int mark = marksParams.getMark();
            if (!userIdToFilmIdWithMark.containsKey(userId)) {
                userIdToFilmIdWithMark.put(userId, new HashMap<>());
            }
            userIdToFilmIdWithMark.get(userId).put(filmId, mark);
        }
        return userIdToFilmIdWithMark;
    }

    @Override
    public List<Film> getRecommendations(List<Integer> filmIdsForRecommendation) {
        String sql = String.format(
                "SELECT f.*, m.name AS mpa_name, mk.rating_count " +
                "FROM films AS f " +
                "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
                "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                            "FROM marks " +
                            "GROUP BY film_id " +
                            "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
                "WHERE f.id IN (%s) " +
                "ORDER BY mk.rating_count DESC NULLS LAST",
                String.join(",", Collections.nCopies(filmIdsForRecommendation.size(), "?")));
        return jdbcTemplate.query(sql, filmListRowMapper(), filmIdsForRecommendation.toArray());
    }

    @Override
    public Map<Integer, List<Genre>> getFilmIdToGenres(List<Integer> filmIdsForRecommendations) {
        String sql = String.format(
                "SELECT fg.film_id, fg.genre_id, g.name " +
                "FROM film_genres AS fg " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (%s) " +
                "ORDER BY fg.film_id",
                String.join(",", Collections.nCopies(filmIdsForRecommendations.size(), "?")));
        List<Map<Integer, Genre>> filmIdToGenreList = jdbcTemplate.query(sql, genresRowMapper(), filmIdsForRecommendations.toArray());
        Map<Integer, List<Genre>> filmIdToGenres = new HashMap<>();
        for (Map<Integer, Genre> filmIdToGenre : filmIdToGenreList) {
            for (Map.Entry<Integer, Genre> e : filmIdToGenre.entrySet()) {
                if (!filmIdToGenres.containsKey(e.getKey())) {
                    filmIdToGenres.put(e.getKey(), new ArrayList<>());
                }
                filmIdToGenres.get(e.getKey()).add(e.getValue());
            }
        }
        return filmIdToGenres;
    }

    @Override
    public Map<Integer, List<Director>> getFilmIdToDirectors(List<Integer> filmIdsForRecommendations) {
        String sql = String.format(
                "SELECT df.film_id, df.director_id, d.director_name " +
                "FROM directors_film AS df " +
                "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
                "WHERE df.film_id IN (%s) " +
                "ORDER BY df.film_id",
                String.join(",", Collections.nCopies(filmIdsForRecommendations.size(), "?")));
        List<Map<Integer, Director>> filmIdToDirectorList = jdbcTemplate.query(sql, directorsRowMapper(), filmIdsForRecommendations.toArray());
        Map<Integer, List<Director>> filmIdToDirectors = new HashMap<>();
        for (Map<Integer, Director> filmIdToDirector : filmIdToDirectorList) {
            for (Map.Entry<Integer, Director> e : filmIdToDirector.entrySet()) {
                if (!filmIdToDirectors.containsKey(e.getKey())) {
                    filmIdToDirectors.put(e.getKey(), new ArrayList<>());
                }
                filmIdToDirectors.get(e.getKey()).add(e.getValue());
            }
        }
        return filmIdToDirectors;
    }

    private RowMapper<Map<Integer, Director>> directorsRowMapper() {
        return (rs, rowNum) -> {
            Map<Integer, Director> filmIdToDirector = new HashMap<>();
            Director director = new Director(rs.getInt("director_id"), rs.getString("director_name"));
            filmIdToDirector.put(rs.getInt("film_id"), director);
            return filmIdToDirector;
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

    private RowMapper<Film> filmListRowMapper() {
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

    private RowMapper<MarksParams> marksRowMapper() {
        return (rs, rowNum) -> new MarksParams(
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("mark")
        );
    }

    private RowMapper<User> usersListRowMapper(Map<Integer, User> usersMap) {
        return (rs, rowNum) -> {
            int userId = rs.getInt("id");
            User user = getNewUser(rs);
            user.setId(userId);
            usersMap.put(userId, user);
            return user;
        };
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = getNewUser(rs);
            user.setId(rs.getInt("id"));
            do {
                if (rs.getInt("friend_id") != 0) {
                    user.getFriends().add(rs.getInt("friend_id"));
                }
            } while (rs.next());
            return user;
        };
    }

    private User getNewUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }
}
