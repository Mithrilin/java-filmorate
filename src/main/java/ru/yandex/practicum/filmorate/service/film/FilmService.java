package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void deleteFilm(Film film);

    void addLike(int id, int userId);

    void deleteLike(int id, int userId);

    List<Film> getPopularFilms(String count);
}
