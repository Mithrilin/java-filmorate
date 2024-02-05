package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventDao {
    Integer addEvent(Event event);

    List<Event> getUserEvents(int userId);
}
