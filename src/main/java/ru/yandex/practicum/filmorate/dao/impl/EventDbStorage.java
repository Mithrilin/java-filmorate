package ru.yandex.practicum.filmorate.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

@Component("EventDbStorage")
public class EventDbStorage implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Event event) {
        String sql = "INSERT INTO event_users " +
                "(user_id, event_type, operation, entity_id, timestamp) " +
                "VALUES(?, ?, ?, ?, ?);";
        jdbcTemplate.update()

    }
}
