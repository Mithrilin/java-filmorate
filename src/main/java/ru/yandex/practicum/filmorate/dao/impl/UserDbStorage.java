package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
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
    public User updateUser(User user) {
        String sql = "update users set login = ?, name = ?, email = ?, birthday = ? where id = ?";
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            statement.setInt(5, user.getId());
            return statement;
        });
        return user;
    }

    @Override
    public List<User> getUserById(int id) {
        String sql = "select * from users u " +
                "left outer join friends f on u.id = f.user_id where u.id = ?";
        return jdbcTemplate.query(sql, userRowMapper(), id);
    }

    @Override
    public List<User> getAllUsers() {
        Map<Integer, User> usersMap = new HashMap<>();
        List<User> users = jdbcTemplate.query("select * from users", (rs, rowNum) -> {
            int userId = rs.getInt("id");
            User user = new User(
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
            user.setId(userId);
            usersMap.put(userId, user);
            return user;
        });
        jdbcTemplate.query("select * from friends", (RowMapper<Integer>) (rs, rowNum) -> {
            int userId;
            do {
                userId = rs.getInt("user_id");
                usersMap.get(userId).getFriends().add(rs.getInt("friend_id"));
            } while (rs.next());
            return null;
        });
        return users;
    }

    @Override
    public void deleteUser(int id) {
        jdbcTemplate.update("delete from users where id = ?", id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        jdbcTemplate.update("insert into friends (user_id, friend_id) values (?, ?);", id, friendId);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        jdbcTemplate.update("delete from friends where user_id = ? and friend_id = ?", id, friendId);
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User(
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
            user.setId(rs.getInt("id"));
            do {
                user.getFriends().add(rs.getInt("friend_id"));
            } while (rs.next());
            return user;
        };
    }
}
