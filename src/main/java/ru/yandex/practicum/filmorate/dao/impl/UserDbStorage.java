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
import java.util.stream.Collectors;

@Repository("userDbStorage")
public class UserDbStorage implements UserDao {
    private static final int MAX_NEGATIVE_MARK_COUNT = 5;
    private static final double CORRECTION_COEFFICIENT = 10;
    private static final String USERS_MARKS_SQL =
            "SELECT * " +
            "FROM marks AS m1 " +
            "WHERE m1.user_id IN (SELECT m2.user_id " +
                                  "FROM marks AS m2 " +
                                  "WHERE m2.film_id IN (SELECT m3.film_id " +
                                                        "FROM marks AS m3 " +
                                                        "WHERE m3.user_id = ?))";
    private static final String RECOMMENDED_FILMS_SQL =
            "SELECT f.*, m.name AS mpa_name, fg.genre_id, g.name AS genre_name, d.*, mk.rating_count " +
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
                            "AND NOT mk2.film_id IN (SELECT film_id " +
                                                     "FROM marks " +
                                                     "WHERE user_id = ?)) " +
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

    /**
     * Получение списка рекомендованных к просмотру фильмов.
     * Алгоритм определяет пользователя с наиболее похожими оценками, затем выбирает из списка положительно
     * оценённых фильмов те, которые не были оценены искомым пользователем.
     * Пользователь с наиболее похожими оценками определяются путём отношения суммы разниц всех оценок к одним и тем же
     * фильмам и корректирующего коэффициента к количеству оценок.
     *
     * @return список рекомендованных к просмотру фильмов.
     */
    @Override
    public List<Film> getRecommendations(int requesterId) {
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark = new HashMap<>();
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff = new HashMap<>();
        Map<Integer, Integer> userIdToMatch = new HashMap<>();
        jdbcTemplate.query(USERS_MARKS_SQL, marksRowMapper(userIdToFilmIdWithMark), requesterId);
        differencesAndMatchesBetweenUsersGetter(userIdToFilmIdWithDiff,
                userIdToFilmIdWithMark,
                userIdToMatch,
                requesterId);
        Integer userIdWithMinDiff = minDiffCountAndUserIdGetter(userIdToFilmIdWithDiff,
                userIdToFilmIdWithMark,
                userIdToMatch,
                requesterId);
        List<Film> recommendations = new ArrayList<>();
        if (userIdWithMinDiff == null) {
            return recommendations;
        }
        jdbcTemplate.query(RECOMMENDED_FILMS_SQL, filmListRowMapper(recommendations), userIdWithMinDiff, requesterId);
        return recommendations;
    }

    private void differencesAndMatchesAdder(Map.Entry<Integer, HashMap<Integer, Integer>> users,
                                            Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                            Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                            Map<Integer, Integer> userIdToMatch,
                                            int requesterId) {
        for (Map.Entry<Integer, Integer> e : users.getValue().entrySet()) {
            int userId = users.getKey();
            if (!userIdToFilmIdWithDiff.containsKey(userId)) {
                userIdToFilmIdWithDiff.put(userId, new HashMap<>());
                userIdToMatch.put(userId, 0);
            }
            int filmId = e.getKey();
            int userMark = e.getValue();
            if (userIdToFilmIdWithMark.get(requesterId).containsKey(filmId)) {
                int requesterMark = userIdToFilmIdWithMark.get(requesterId).get(filmId);
                userIdToFilmIdWithDiff.get(userId).put(filmId, Math.abs(requesterMark - userMark));
                int newMatchCount = userIdToMatch.get(userId) + 1;
                userIdToMatch.put(userId, newMatchCount);
            }
        }
    }

    private void differencesAndMatchesBetweenUsersGetter(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                                         Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                                         Map<Integer, Integer> userIdToMatch,
                                                         int requesterId) {
        for (Map.Entry<Integer, HashMap<Integer, Integer>> users : userIdToFilmIdWithMark.entrySet()) {
            if (users.getKey() == requesterId) {
                continue;
            }
            differencesAndMatchesAdder(users,
                    userIdToFilmIdWithDiff,
                    userIdToFilmIdWithMark,
                    userIdToMatch,
                    requesterId);
        }
    }

    private Integer minDiffCountAndUserIdGetter(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                             Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                             Map<Integer, Integer> userIdToMatch,
                                             int requesterId) {
        double minDiffCount = Double.MAX_VALUE;
        Integer userIdWithMinDiff = null;
        for (Map.Entry<Integer, HashMap<Integer, Integer>> users : userIdToFilmIdWithDiff.entrySet()) {
            int checkedUserId = users.getKey();
            if (userIdToMatch.get(checkedUserId) == 0
                    || isUserHaveNotFilmsForRecommendations(userIdToFilmIdWithMark, requesterId, checkedUserId)) {
                continue;
            }
            int sumDiff = 0;
            for (Integer e : users.getValue().values()) {
                sumDiff += e;
            }
            double diffCount = (sumDiff + CORRECTION_COEFFICIENT) / userIdToMatch.get(checkedUserId);

            if ((diffCount < minDiffCount)
                    || ((diffCount == minDiffCount) && (userIdToMatch.get(checkedUserId) > userIdToMatch.get(userIdWithMinDiff)))) {
                minDiffCount = diffCount;
                userIdWithMinDiff = checkedUserId;
            }
        }
        return userIdWithMinDiff;
    }

    private boolean isUserHaveNotFilmsForRecommendations(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                                         int requesterId,
                                                         int checkedUserId) {
        Map<Integer, Integer> requesterMarksMap = userIdToFilmIdWithMark.get(requesterId);
        Map<Integer, Integer> checkedUserMarksMap = userIdToFilmIdWithMark.get(checkedUserId);
        List<Integer> filmsIdWithPositiveMark = checkedUserMarksMap.entrySet().stream()
                .filter(filmIdToMarkMap -> !requesterMarksMap.containsKey(filmIdToMarkMap.getKey())
                        && filmIdToMarkMap.getValue() > MAX_NEGATIVE_MARK_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return filmsIdWithPositiveMark.isEmpty();
    }

    private RowMapper<Film> filmListRowMapper(List<Film> films) {
        Map<Integer, Film> filmMap = new HashMap<>();
        Map<Integer, HashMap<Integer, Genre>> filmIdToGenreMap = new HashMap<>();
        Map<Integer, HashMap<Integer, Director>> filmIdToDirectorMap = new HashMap<>();
        return (rs, rowNum) -> {
            int filmId = rs.getInt("id");
            if (!filmMap.containsKey(filmId)) {
                Film film = new Film(
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("releasedate").toLocalDate(),
                        rs.getInt("duration"),
                        new Mpa(rs.getInt("mpa_id"),
                                rs.getString("mpa_name")));
                film.setRating(rs.getDouble("rating_count"));
                film.setId(filmId);
                filmMap.put(filmId, film);
                films.add(film);
                filmIdToGenreMap.put(filmId, new HashMap<>());
                filmIdToDirectorMap.put(filmId, new HashMap<>());
            }
            int genreId = rs.getInt("genre_id");
            if (genreId != 0 && !filmIdToGenreMap.get(filmId).containsKey(genreId)) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                filmIdToGenreMap.get(filmId).put(genreId, genre);
                filmMap.get(filmId).getGenres().add(genre);
            }
            int directorId = rs.getInt("director_id");
            if (directorId != 0 && !filmIdToDirectorMap.get(filmId).containsKey(directorId)) {
                Director director = new Director(directorId, rs.getString("director_name"));
                filmIdToDirectorMap.get(filmId).put(directorId, director);
                filmMap.get(filmId).getDirectors().add(director);
            }
            return null;
        };
    }

    private RowMapper<Film> marksRowMapper(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark) {
        return (rs, rowNum) -> {
            int userId = rs.getInt("user_id");
            int filmId = rs.getInt("film_id");
            int mark = rs.getInt("mark");
            if (!userIdToFilmIdWithMark.containsKey(userId)) {
                userIdToFilmIdWithMark.put(userId, new HashMap<>());
            }
            if (!userIdToFilmIdWithMark.get(userId).containsKey(filmId)) {
                userIdToFilmIdWithMark.get(userId).put(filmId, mark);
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
