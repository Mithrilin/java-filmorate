package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public class DirectorController {

    public List<Director> getDirectors() {
        log.info("Получены все режиссеры");
        return directorDao.getDirectors();
    }

    public List<Director> getDirectorById(Integer id) {
        Director director = directorDao.getDirectorById(id).get(0);
        log.info("Получен режиссер = {}", director);
        return null;
    }

    public Director addDirector(Director director) {
        Director addDirector = directorDao.addDirector(director);
        log.info("Добавлен новый режиссер = {}", addDirector);
        return addDirector;
    }

    public Director updateDirector(Director director) {
        Director updateDirector = directorDao.updateDirector(director);
        log.info("Обновлен режиссер = {}", updateDirector);
        return updateDirector;
    }

    public void deleteDirector(Integer id) {
        directorDao.deleteDirector(id);
        log.info("Удален режиссер под id = {}", id);
    }
}
