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

    List<Film> getPopularFilms(Integer count);

    List<Film> getPopularFilmsByYear(Integer count, Integer year);

    List<Film> getPopularFilmsByGenre(Integer count, Integer genreId);

    List<Film> getPopularFilmsByYearAndGenre(Integer count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getFilmsByDirectorIdSortByYear(int directorId);

    List<Film> getFilmsByDirectorIdSortByLikes(int directorId);

    List<Film> getFilmsByTitleSearch(String query);

    List<Film> getFilmsByDirectorSearch(String query);

    List<Film> getFilmsByTitleAndDirectorSearch(String query);

}
