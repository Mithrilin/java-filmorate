package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void addLike(int id, int userId);

    void deleteLike(int id, int userId);

    void deleteFilm(int id);

    List<Film> getPopularFilms(String count, String genreId, String year);

    List<Film> getCommonFilms(int userId, int friendId);
}
