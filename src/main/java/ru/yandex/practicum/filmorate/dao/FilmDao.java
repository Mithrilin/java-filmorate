package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDao {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilmById(int id);

    List<Film> getAllFilms();

    void addLike(int id, int userId);

    Integer deleteLike(int id, int userId);

    Integer deleteFilm(int id);

    List<Film> getPopularFilms(String count);

    List<Film> getFilmsSortByDirectorId(int directorId, String sortBy);//GET /films/director/{directorId}?sortBy=[year,likes]

    List<Film> getCommonFilms(int userId, int friendId);
}
