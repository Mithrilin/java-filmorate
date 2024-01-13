package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Optional;

public interface GenreDao {
    Optional<Genre> getGenreById(int id);
}
