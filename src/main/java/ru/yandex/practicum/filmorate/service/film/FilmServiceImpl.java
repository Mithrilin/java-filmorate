package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.FilmDao;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Component
public class FilmServiceImpl implements FilmService {
    private static final LocalDate INITIAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmDao filmDao;

    public FilmServiceImpl(@Qualifier("filmDbStorage") FilmDao filmStorage) {
        this.filmDao = filmStorage;
    }

    @Override
    public Film addFilm(Film film) {
        isFilmValid(film);
        film = filmDao.addFilm(film);
        log.info("Добавлен новый фильм с ID = {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        isFilmValid(film);
        filmDao.updateFilm(film);
        log.info("Фильм с ID {} обновлён.", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        List<Film> films = filmDao.getFilmById(id);
        if (films.size() == 0) {
            throw new NotFoundException("Фильм с id " + id + " не найден.");
        }
        log.info("Фильм с id {} возвращён.", id);
        return films.get(0);
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = filmDao.getAllFilms();
        log.info("Текущее количество фильмов: {}. Список возвращён.", films.size());
        return films;
    }

    @Override
    public void addLike(int id, int userId) {
        filmDao.addLike(id, userId);
        log.info("Пользователь с id {} лайкнул фильм с id {}.", userId, id);
    }

    @Override
    public void deleteLike(int id, int userId) {
        Integer result = filmDao.deleteLike(id, userId);
        if (result == 0) {
            throw new NotFoundException("Фильм с id " + id + "или пользователь с id " + userId + " не найдены.");
        }
        log.info("Пользователь с id {} удалил лайк к фильму с id {}.", userId, id);
    }

    @Override
    public List<Film> getPopularFilms(String count) {
        List<Film> films = filmDao.getPopularFilms(count);
        log.info("Список популярных фильмов возвращён.");
        return films;
    }

    private void isFilmValid(Film film) {
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            throw new ValidationException("Фильм не прошёл валидацию. Дата релиза меньше минимального значения.");
        }
    }
}