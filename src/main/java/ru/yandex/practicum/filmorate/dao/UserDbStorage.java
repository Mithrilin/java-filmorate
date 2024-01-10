package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        jdbcTemplate.update("insert into users (login, name, email, birthday) values (?, ?, ?, ?);",
                user.getLogin(), user.getName(), user.getEmail(), user.getBirthday());
        KeyHolder key = new GeneratedKeyHolder();
        String sql = "insert into users (login, name, email, birthday) values (?, ?, ?, ?);";

        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    return null;
            }
        }, key);
        user.setId(key.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        return null;
    }

    @Override
    public void deleteUser(User user) {

    }

    @Override
    public Map<Integer, User> getAllUsers() {
        return null;
    }

    @Override
    public User getUserById(int id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?", (rs, rowNum) -> {
            User user = new User(
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
            user.setId(rs.getInt("id"));
            return user;
        }, id);
    }
}
