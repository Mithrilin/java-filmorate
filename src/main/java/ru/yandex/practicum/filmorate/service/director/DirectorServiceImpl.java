package ru.yandex.practicum.filmorate.service.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
public class DirectorServiceImpl implements DirectorService {

    private final DirectorDao directorDao;

    public DirectorServiceImpl(@Qualifier("directorDbStorage") DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

    @Override
    public List<Director> getDirectors() {
        log.info("Получены все режиссеры");
        return directorDao.getDirectors();
    }

    @Override
    public Director getDirectorById(int id) {
        List<Director> listDirector = directorDao.getDirectorById(id);
        if (listDirector.isEmpty()) {
            throw new NotFoundException("Не найден режиссер с id = " + id);
        }
        log.info("Получен режиссер с ид {}", id);
        return listDirector.get(0);
    }

    @Override
    public Director addDirector(Director director) {
        Director addDirector = directorDao.addDirector(director);
        log.info("Добавлен новый режиссер = {}", addDirector);
        return addDirector;
    }

    @Override
    public Director updateDirector(Director director) {
        int directorId = director.getId();
        Director upDirector = directorDao.updateDirector(director);
        if (upDirector == null) {
            throw new NotFoundException("Директор с id " + directorId + " не найден.");
        }
        log.info("Обновлен режиссер с ид {}", directorId);
        return upDirector;
    }

    @Override
    public void deleteDirector(int id) {
        int check = directorDao.deleteDirector(id);
        if (check == 0) {
            throw new NotFoundException("Режиссер не найден под id = " + id);
        }
        log.info("Удален режиссер под id = {}", id);
    }
}
