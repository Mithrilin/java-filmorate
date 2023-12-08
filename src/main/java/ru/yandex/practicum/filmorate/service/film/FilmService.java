package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    void deleteFilm(Film film);

    List<Film> getAllFilms();

    void addLike(int id, int userId);

    void deleteLike(int id, int userId);
}
