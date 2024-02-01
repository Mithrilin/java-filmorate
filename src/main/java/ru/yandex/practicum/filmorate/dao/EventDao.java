package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Event;

public interface EventDao {
    void addEvent(Event event);

    Event getEvent(int eventId);
}
