package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Objects;

@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        KeyHolder key = new GeneratedKeyHolder();
        String sqlQuery = "insert into users (login, name, email, birthday) values (?, ?, ?, ?);";

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlQuery, new String[]{"id"});
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            return statement;
        }, key);

        user.setId(Objects.requireNonNull(key.getKey()).intValue());
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
