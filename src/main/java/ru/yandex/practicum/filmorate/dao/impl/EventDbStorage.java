package ru.yandex.practicum.filmorate.dao.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;

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
        String sql = "merge into users_events as ue1 " +
                "using (values(?, ?, ?, ?, ?)) as ue2 (user_id, event_type, operation, entity_id, timestamp) " +
                "ON ue1.user_id=ue2.user_id and ue1.entity_id=ue2.entity_id and ue1.event_type=ue2.event_type " +
                "and ue1.operation=ue2.operation " +
                "when matched then update set ue1.user_id=ue2.user_id and ue1.entity_id=ue2.entity_id " +
                "and ue1.event_type=ue2.event_type and ue1.operation=ue2.operation " +
                "when not matched then insert (user_id, event_type, operation, entity_id, timestamp) " +
                "values (ue2.user_id, ue2.event_type, ue2.operation, ue2.entity_id, ue2.timestamp);";
        return jdbcTemplate.update(sql, event.getUserId(), event.getEventType(),
                event.getOperation(), event.getEntityId(), event.getTimestamp());
    }

    @Override
    public List<Event> getUserEvents(int userId) {
        if (!isUserInDb(userId)) {
            throw new NotFoundException(String.format("Пользователь с идентификатором %s не найден",userId));
        }
        String sql = "select * from users_events where user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Event(
                rs.getInt("event_id"),
                rs.getInt("user_id"),
                rs.getString("event_type"),
                rs.getString("operation"),
                rs.getInt("entity_id"),
                rs.getLong("timestamp")), userId);
    }

    private boolean isUserInDb(int userId) {
        String sql = "select * from users where id = ?";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User(rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getDate("birthday").toLocalDate());
            user.setId(userId);
            return user;
        }, userId);

        return !users.isEmpty();
    }
}
