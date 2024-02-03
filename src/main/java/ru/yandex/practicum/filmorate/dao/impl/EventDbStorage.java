package ru.yandex.practicum.filmorate.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Component("EventDbStorage")
public class EventDbStorage implements EventDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer addEvent(Event event) {
        String sql = "insert into users_events " +
                "(user_id, event_type, operation, entity_id, timestamp) " +
                "values(?, ?, ?, ?, ?);";
        return jdbcTemplate.update(sql, event.getUserId(), event.getEventType(),
                event.getOperation(), event.getEntityId(), event.getTimeStamp());
    }

    @Override
    public List<Event> getUserEvents(int userId) {
        String sql = "select * from users_events where user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Event(
                rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getInt("entity_id"),
                rs.getLong("timestamp")), userId);
    }
}
