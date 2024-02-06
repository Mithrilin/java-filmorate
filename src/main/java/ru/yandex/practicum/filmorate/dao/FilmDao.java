package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDao {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    List<Film> getFilmById(int id);

    List<Film> getAllFilms();

    void addMark(int id, int userId, String mark);

    Integer deleteMark(int id, int userId);

    Integer deleteFilm(int id);

    List<Film> getPopularFilms();

    List<Film> getPopularFilmsWithLimit(Integer count);

    List<Film> getPopularFilmsByYear(Integer year);

    List<Film> getPopularFilmsByYearWithLimit(Integer count, Integer year);

    List<Film> getPopularFilmsByGenre(Integer genreId);

    List<Film> getPopularFilmsByGenreWithLimit(Integer count, Integer genreId);

    List<Film> getPopularFilmsByYearAndGenre(Integer genreId, Integer year);

    List<Film> getPopularFilmsByYearAndGenreWithLimit(Integer count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getFilmsByDirectorIdSortByYear(int directorId);

    List<Film> getFilmsByDirectorIdSortByLikes(int directorId);

    List<Film> getFilmsByTitleSearch(String query);

    List<Film> getFilmsByDirectorSearch(String query);

    List<Film> getFilmsByTitleAndDirectorSearch(String query);

}
