package ru.yandex.practicum.filmorate.service.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;

    @Autowired
    public EventServiceImpl(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public Integer addEvent(Event event) {
        return eventDao.addEvent(event);
    }

    @Override
    public List<Event> getUserEvents(int userId) {
        return eventDao.getUserEvents(userId);
    }
}
