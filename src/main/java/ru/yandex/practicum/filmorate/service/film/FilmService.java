package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film getFilmById(int id);

    List<Film> getAllFilms();

    void addMark(int id, int userId, int mark);

    void deleteMark(int id, int userId);

    void deleteFilm(int id);

    List<Film> getPopularFilms(Integer count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getFilmsByDirectorId(int directorId, String sortBy);

    List<Film> getFilmsBySearch(String query, List<String> params);
}