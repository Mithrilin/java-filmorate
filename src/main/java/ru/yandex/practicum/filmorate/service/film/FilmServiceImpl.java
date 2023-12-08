package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FilmServiceImpl implements FilmService {
    private static final LocalDate INITIAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;

    @Override
    public Film addFilm(Film film) {
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            log.error("Фильм не прошёл валидацию.");
            throw new ValidationException("Фильм не прошёл валидацию.");
        }
        log.info("Добавлен новый фильм с ID = {}", film.getId());
        return filmStorage.addFilm(film);
    }

    @Override
    public Film updateFilm(Film film) {
        isIdValid(film.getId());
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            log.error("Фильм не прошёл валидацию.");
            throw new ValidationException("Фильм не прошёл валидацию.");
        }
        log.info("Фильм с ID {} обновлён.", film.getId());
        return filmStorage.updateFilm(film);
    }

    @Override
    public void deleteFilm(Film film) {
        log.info("Фильм с ID {} удалён.", film.getId());
        filmStorage.deleteFilm(film);
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms().values());
        log.info("Текущее количество фильмов: {}", films.size());
        return films;
    }

    @Override
    public void addLike(int id, int userId) {
        Film film = isIdValid(id);
        log.info("Пользователь с id {} лайкнул фильм с id {}.", userId, id);
        film.getLikes().add(userId);
    }

    @Override
    public void deleteLike(int id, int userId) {
        Film film = isIdValid(id);
        log.info("Пользователь с id {} удалил лайк к фильму с id {}.", userId, id);
        film.getLikes().remove(userId);
    }

    @Override
    public Film getFilmById(int id) {
        return isIdValid(id);
    }

    private Film isIdValid(int id) {
        if (!filmStorage.getAllFilms().containsKey(id)) {
            log.error("Фильм с id " + id + " не найден.");
            throw new FilmNotFoundException("Фильм с id " + id + " не найден.");
        }
        return filmStorage.getAllFilms().get(id);
    }
}