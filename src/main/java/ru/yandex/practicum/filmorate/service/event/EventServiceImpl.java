package ru.yandex.practicum.filmorate.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventDao eventDao;

    public EventServiceImpl(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public Integer addEvent(Event event) {
        int i = eventDao.addEvent(event);
        log.info("Событие пользователя с ид = {} добавлено", event.getUserId());
        return i;
    }

    @Override
    public List<Event> getUserEvents(int userId) {
        List<Event> events = eventDao.getUserEvents(userId);
        log.info("Список с активностью пользователя с ид {} возвращён.", userId);
        return events;
    }
}
