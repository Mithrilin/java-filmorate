package ru.yandex.practicum.filmorate.service.event;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventService {
    Integer addEvent(Event event);

    List<Event> getUserEvents(int userId);

}
