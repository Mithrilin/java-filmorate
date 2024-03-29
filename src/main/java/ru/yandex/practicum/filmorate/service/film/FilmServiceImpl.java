package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.SearchParameterException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.event.EventService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
    private static final LocalDate INITIAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    public static final String DIRECTOR_PARAM = "director";
    public static final String TITLE_PARAM = "title";
    private final FilmDao filmDao;
    private final EventService eventService;

    public FilmServiceImpl(@Qualifier("filmDbStorage") FilmDao filmStorage, EventService eventService) {
        this.filmDao = filmStorage;
        this.eventService = eventService;
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
        int filmId = film.getId();
        film = filmDao.updateFilm(film);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден.");
        }
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
    public void addMark(int id, int userId, int mark) {
        filmDao.addMark(id, userId, mark);
        eventService.addEvent(new Event(userId, "MARK", "ADD", id));
        log.info("Пользователь с id {} лайкнул фильм с id {}.", userId, id);
    }

    @Override
    public void deleteMark(int id, int userId) {
        Integer result = filmDao.deleteMark(id, userId);
        if (result == 0) {
            throw new NotFoundException("Фильм с id " + id + " или пользователь с id " + userId + " не найдены.");
        }
        eventService.addEvent(new Event(userId, "MARK", "REMOVE", id));
        log.info("Пользователь с id {} удалил лайк к фильму с id {}.", userId, id);
    }

    @Override
    public void deleteFilm(int id) {
        Integer result = filmDao.deleteFilm(id);
        if (result == 0) throw new NotFoundException("Фильм с id " + id + " не найден.");
        log.info("Фильм с id {} удален", id);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        List<Film> films;
        if (genreId == 0 && year == null && count == null) {
            films = filmDao.getPopularFilms();
            log.info("Список популярных фильмов без лимитов возвращён.");
        } else if (genreId == 0 && year == null) {
            films = filmDao.getPopularFilmsWithLimit(count);
            log.info("Список популярных фильмов размером {} возвращён.", count);
        } else if (genreId != 0 && year == null && count == null) {
            films = filmDao.getPopularFilmsByGenre(genreId);
            log.info("Список популярных фильмов жанра с ид {} без лимита возвращён.", genreId);
        } else if (genreId != 0 && year == null) {
            films = filmDao.getPopularFilmsByGenreWithLimit(count, genreId);
            log.info("Список популярных фильмов с жанром с ид {} размером {} возвращён.", genreId, count);
        } else if (genreId != 0 && count == null) {
            films = filmDao.getPopularFilmsByYearAndGenre(genreId, year);
            log.info("Список популярных фильмов {} года с жанром с ид {} возвращён.", year, genreId);
        } else if (genreId != 0) {
            films = filmDao.getPopularFilmsByYearAndGenreWithLimit(count, genreId, year);
            log.info("Список популярных фильмов {} года с жанром с ид {} размером {} возвращён.", year, genreId, count);
        } else if (count == null) {
            films = filmDao.getPopularFilmsByYear(year);
            log.info("Список популярных фильмов {} года возвращён.", year);
        } else {
            films = filmDao.getPopularFilmsByYearWithLimit(count, year);
            log.info("Список популярных фильмов {} года размером {} возвращён.", year, count);
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        List<Film> commonFilms = filmDao.getCommonFilms(userId, friendId);
        log.info("Список общих фильмов пользователей с id{} и id{}.", userId, friendId);
        return commonFilms;
    }

    @Override
    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        switch (sortBy) {
            case "year":
                return filmDao.getFilmsByDirectorIdSortByYear(directorId);
            case "likes":
                return filmDao.getFilmsByDirectorIdSortByLikes(directorId);
            default:
                throw new NotFoundException("Параметр сортировки не определен! Параметр сортировки = " + sortBy);
        }
    }

    @Override
    public List<Film> getFilmsBySearch(String query, List<String> params) {
        if (params.size() == 1) {
            switch (params.get(0)) {
                case TITLE_PARAM:
                    log.info("Результат поиска фильма по части названия {}", query);
                    return filmDao.getFilmsByTitleSearch(query);
                case DIRECTOR_PARAM:
                    log.info("Результат поиска фильма по части названия {}", query);
                    return filmDao.getFilmsByDirectorSearch(query);
                default:
                    return new ArrayList<>();
            }
        } else if (params.size() == 2 && params.containsAll(List.of(TITLE_PARAM, DIRECTOR_PARAM))) {
            log.info("Результат поиска фильма по части названия {}", query);
            return filmDao.getFilmsByTitleAndDirectorSearch(query);
        } else {
            throw new SearchParameterException("Параметр поиска не определён.");
        }
    }

    // Метод проверки минимальной даты
    private void isFilmValid(Film film) {
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            throw new ValidationException("Фильм не прошёл валидацию. Дата релиза меньше минимального значения.");
        }
    }
}