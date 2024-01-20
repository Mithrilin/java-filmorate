package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilmById(int id);

    List<Film> getAllFilms();

    void addLike(int id, int userId);

    Integer deleteLike(int id, int userId);

    List<Film> getPopularFilms(String count);
}
