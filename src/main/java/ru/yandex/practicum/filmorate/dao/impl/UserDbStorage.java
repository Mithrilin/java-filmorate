package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("userDbStorage")
public class UserDbStorage implements UserDao {
    private static final String ALL_USERS_MARKS_SQL =
            "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, d.*, " +
                    "m2.user_id, m2.mark , mk.rating_count " +
            "FROM films AS f " +
            "LEFT JOIN mpa AS m ON f.mpa_id = m.id " +
            "LEFT JOIN film_genres AS fg ON f.id = fg.film_id " +
            "LEFT JOIN genres AS g ON fg.genre_id = g.id " +
            "LEFT JOIN directors_film AS df ON f.id = df.film_id " +
            "LEFT JOIN directors AS d ON df.director_id = d.director_id " +
            "LEFT JOIN marks AS m2 ON f.id = m2.film_id " +
            "LEFT JOIN (SELECT film_id, CAST(sum(mark) AS DECIMAL(3,1))/CAST(count(film_id) AS DECIMAL(3,1)) AS rating_count " +
                        "FROM marks " +
                        "GROUP BY film_id " +
                        "ORDER BY rating_count DESC) AS mk ON f.id = mk.film_id " +
            "ORDER BY mk.rating_count DESC NULLS LAST";
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
    public List<Film> getRecommendations(int id) {
        Map<Integer, Double> filmIdToMarkForUser = new HashMap<>();
        // Список пользователей с оценками, которые оценили те же фильмы
        Map<Integer, HashMap<Integer, Double>> allUsersMarks = new HashMap<>();
        // Различия между оценками пользователей с целевым пользователем
        Map<Integer, HashMap<Integer, Double>> diff = new HashMap<>();
        Map<Integer, Integer> match = new HashMap<>();
        Map<Integer, Film> filmByIds = new HashMap<>();
        jdbcTemplate.query(ALL_USERS_MARKS_SQL, recommendedFilmsRowMapper(filmByIds, filmIdToMarkForUser, allUsersMarks, id));

        for (Map.Entry<Integer, HashMap<Integer, Double>> users : allUsersMarks.entrySet()) {
            for (Map.Entry<Integer, Double> e : users.getValue().entrySet()) {
                // ид пользователя
                int userId = users.getKey();
                if (!diff.containsKey(userId)) {
                    diff.put(userId, new HashMap<>());
                    match.put(userId, 0);
                }
                // ИД фильма
                int filmId = e.getKey();
                // Оценка
                double mark = e.getValue();
                if (filmIdToMarkForUser.containsKey(filmId)) {
                    diff.get(userId).put(filmId, filmIdToMarkForUser.get(filmId) - mark);
                    int newMatch = match.get(userId) + 1;
                    match.put(userId, newMatch);
                }
            }
        }
        int userMinDiff = 0;
        double minDiffCount = Double.MAX_VALUE;
        for (Map.Entry<Integer, HashMap<Integer, Double>> users : diff.entrySet()) {
            double sumDiff = 0;
            for (Double e : users.getValue().values()) {
                sumDiff += e;
            }
            double count = Math.abs(sumDiff / match.get(users.getKey()));
            if (count < minDiffCount) {
                minDiffCount = count;
                userMinDiff = users.getKey();
            }
        }
        List<Film> recommendations = new ArrayList<>();
        Map<Integer, Double> recommendationMap = allUsersMarks.get(userMinDiff);
        for (Map.Entry<Integer, Double> e : recommendationMap.entrySet()) {
            if (!filmIdToMarkForUser.containsKey(e.getKey())) {
                if (e.getValue() > 5) {
                    recommendations.add(filmByIds.get(e.getKey()));
                }
            }
        }
        return recommendations;
    }

    private RowMapper<Film> recommendedFilmsRowMapper(Map<Integer, Film> filmByIds,
                                                      Map<Integer, Double> filmIdToMarkForUser,
                                                      Map<Integer, HashMap<Integer, Double>> allUsersMarks,
                                                      int id) {
        Map<Integer, HashMap<Integer, Genre>> filmGenreMap = new HashMap<>();
        Map<Integer, HashMap<Integer, Director>> filmDirectorMap = new HashMap<>();
        return (rs, rowNum) -> {
            int userId = rs.getInt("user_id");
            int filmId = rs.getInt("id");
            double mark = rs.getDouble("mark_count");

            if (!filmByIds.containsKey(filmId)) {
                Film film = new Film(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("releasedate").toLocalDate(),
                        rs.getInt("duration"),
                        new Mpa(rs.getInt("mpa_id"),
                                rs.getString("mpa_name")));
                film.setRating(rs.getDouble("rating_count"));
                film.setId(filmId);
                filmByIds.put(filmId, film);
                filmGenreMap.put(filmId, new HashMap<>());
                filmDirectorMap.put(filmId, new HashMap<>());
            }
            int genreId = rs.getInt("genre_id");
            if (genreId != 0 && !filmGenreMap.get(filmId).containsKey(genreId)) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                filmGenreMap.get(filmId).put(genreId, genre);
                filmByIds.get(filmId).getGenres().add(genre);
            }
            int directorId = rs.getInt("director_id");
            if (directorId != 0 && !filmDirectorMap.get(filmId).containsKey(directorId)) {
                Director director = new Director(directorId, rs.getString("director_name"));
                filmDirectorMap.get(filmId).put(directorId, director);
                filmByIds.get(filmId).getDirectors().add(director);
            }
            if (userId == id) {
                if (!filmIdToMarkForUser.containsKey(filmId)) {
                    filmIdToMarkForUser.put(filmId, mark);
                }
            } else {
                if (!allUsersMarks.containsKey(userId)) {
                    allUsersMarks.put(userId, new HashMap<>());
                }
                if (!allUsersMarks.get(userId).containsKey(filmId)) {
                    allUsersMarks.get(userId).put(filmId, mark);
                }
            }
            return null;
        };
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
