package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Component
public class FilmServiceImpl implements FilmService {
    private static final LocalDate INITIAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;

    public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @Override
    public Film addFilm(Film film) {
        isFilmValid(film);
        film = filmStorage.addFilm(film);
        log.info("Добавлен новый фильм с ID = {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        isFilmValid(film);
        filmStorage.updateFilm(film);
        log.info("Фильм с ID {} обновлён.", film.getId());
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        log.info("Фильм с id {} возвращён.", id);
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        log.info("Текущее количество фильмов: {}. Список возвращён.", films.size());
        return films;
    }

    @Override
    public void addLike(int id, int userId) {
        filmStorage.addLike(id, userId);
        log.info("Пользователь с id {} лайкнул фильм с id {}.", userId, id);
    }

    @Override
    public void deleteLike(int id, int userId) {
        filmStorage.deleteLike(id, userId);
        log.info("Пользователь с id {} удалил лайк к фильму с id {}.", userId, id);
    }

    @Override
    public List<Film> getPopularFilms(String count) {
        List<Film> films = new ArrayList<>();
        if (count != null) {
            int length = Integer.parseInt(count);
            films = filmStorage.getPopularFilms(length);
            log.info("Список популярных фильмов размером {} возвращён.", length);
        } else {
//            films = filmStorage.getMostPopularFilm();
            log.info("Самый популярный фильм с id {} возвращён.", films.get(0).getId());
        }

        return films;
    }

    @Override
    public void deleteFilm(Film film) {
        log.info("Фильм с ID {} удалён.", film.getId());
        filmStorage.deleteFilm(film);
    }

    private void isFilmValid(Film film) {
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            log.error("Фильм не прошёл валидацию. Дата релиза меньше минимального значения.");
            throw new ValidationException("Фильм не прошёл валидацию.");
        }
    }
}