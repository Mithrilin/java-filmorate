package ru.yandex.practicum.filmorate.service.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Slf4j
@Service
@Component
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

        Director director = directorDao.getDirectorById(id);

        log.info("Получен режиссер = {}", director);
        return director;
    }

    @Override
    public Director addDirector(Director director) {
        Director addDirector = directorDao.addDirector(director);
        log.info("Добавлен новый режиссер = {}", addDirector);
        return addDirector;
    }

    @Override
    public Director updateDirector(Director director) {
        Director upDirector = directorDao.updateDirector(director);
        log.info("Обновлен режиссер = {}", upDirector);
        return upDirector;
    }

    @Override
    public void deleteDirector(int id) {
        directorDao.deleteDirector(id);
        log.info("Удален режиссер под id = {}", id);
    }

}
