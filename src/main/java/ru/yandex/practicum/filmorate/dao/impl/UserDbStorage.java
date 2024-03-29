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
