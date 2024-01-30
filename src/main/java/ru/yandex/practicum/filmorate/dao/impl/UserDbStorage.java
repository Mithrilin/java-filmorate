package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserDao;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Component("userDbStorage")
public class UserDbStorage implements UserDao {
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
        // Пользователи, которые вообще лайкали фильмы
        List<Integer> usersId = new ArrayList<>();
        // Список фильмов, которые лайкнул целевой пользователь
        Map<Integer, Film> usersFavoriteFilms = new HashMap<>();
        // Список всех фильмов, у которых есть лайки
        Map<Integer, Film> filmMap = new HashMap<>();
        // Общий список всех фильмов с лайками по пользователям
        Map<Integer, HashMap<Integer, Film>> allLikes = new HashMap<>();
        String sql = "select l.user_id, l.film_id, " +
                "f.name as film_name, f.releasedate, f.description, f.duration, f.mpa_id, " +
                "m.name as mpa_name, fg.genre_id, g.name as genre_name " +
                "from likes l " +
                "join films f on f.id = l.film_id " +
                "join mpa m on f.mpa_id = m.id " +
                "left outer join film_genres fg on f.id = fg.film_id " +
                "left outer join genres g on fg.genre_id = g.id " +
                "order by l.user_id, l.film_id;";
        // Собираем все списки
        jdbcTemplate.query(sql, likedFilmsRowMapper(allLikes, usersId, filmMap, usersFavoriteFilms, id));
        // Проставляем количество лайков к каждому фильму
        jdbcTemplate.query("select film_id, count(user_id) from likes group by film_id order by film_id;",
                (RowMapper<Film>) (rs, rowNum) -> {
                    do {
                        filmMap.get(rs.getInt("film_id")).setLike(rs.getInt("count(user_id)"));
                    } while (rs.next());
                    return null;
                });
        // Максимальное количество совпадений
        int matchesMax = 0;
        // Список рекомендаций
        List<Film> recommendations = new ArrayList<>();
        // Проходим по списку пользователей
        for (Integer userId : usersId) {
            // Проверяем, что пользователь не целевой
            if (userId != id) {
                HashMap<Integer, Film> likedFilms = allLikes.get(userId);
                // Количество совпадений лайков с конкретным пользователем
                int matches = 0;
                List<Film> recommendationsTemp = new ArrayList<>();
                // Проходим по списку фильмов пользователя
                for (Integer filmId : likedFilms.keySet()) {
                    // Проверяем совпадает ли лайк с целевым пользователем
                    if (usersFavoriteFilms.containsKey(filmId)) {
                        // Если совпадает, то увеличиваем счётчик
                        matches++;
                    } else {
                        // Если не совпадает, добавляем во временные рекомендации
                        recommendationsTemp.add(filmMap.get(filmId));
                    }
                }
                // Проверяем, что у пользователя помимо совпавших фильмов есть и фильмы для рекомендации
                // И что у этого пользователя наибольшее количество совпадений
                if (matches < likedFilms.size() && matches > matchesMax) {
                    matchesMax = matches;
                    recommendations = recommendationsTemp;
                }
            }
        }
        return recommendations;
    }

    private RowMapper<Object> likedFilmsRowMapper(Map<Integer, HashMap<Integer, Film>> allLikes,
                                                  List<Integer> usersId,
                                                  Map<Integer, Film> filmMap,
                                                  Map<Integer, Film> usersFavoriteFilms,
                                                  int id) {
        return (rs, rowNum) -> {
            int userId = rs.getInt("user_id");
            // Проверяем есть ли такой пользователь в списке
            if (!allLikes.containsKey(userId)) {
                allLikes.put(userId, new HashMap<>());
                usersId.add(userId);
            }
            int filmId = rs.getInt("film_id");
            // Проверяем есть ли этот фильм в списке лайков пользователя
            if (!allLikes.get(userId).containsKey(filmId)) {
                // Проверяем есть ли этот фильм в списке фильмов
                if (!filmMap.containsKey(filmId)) {
                    Film film = new Film(
                            rs.getString("film_name"),
                            rs.getString("description"),
                            rs.getDate("releasedate").toLocalDate(),
                            rs.getInt("duration"),
                            new Mpa(rs.getInt("mpa_id"),
                                    rs.getString("mpa_name")));
                    film.setId(filmId);
                    filmMap.put(filmId, film);
                }
                allLikes.get(userId).put(filmId, filmMap.get(filmId));
                // Проверяем является ли данный пользователь целевым
                if (userId == id) {
                    usersFavoriteFilms.put(filmId, filmMap.get(filmId));
                }
            }
            Genre genre = new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("genre_name"));
            // Проверяем есть ли у фильма жанры
            if (genre.getId() != 0) {
                allLikes.get(userId).get(filmId).getGenres().add(genre);
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
