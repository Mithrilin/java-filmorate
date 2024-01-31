package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorDao {
    List<Director> getDirectors (); //GET /directors - Список всех режиссёров

    List<Director> getDirectorById(int id); //GET /directors/{id}- Получение режиссёра по id

    Director addDirector(Director director); //POST /directors - Создание режиссёра

    Director updateDirector(Director director); //PUT /directors - Изменение режиссёра

    void deleteDirector(int id); //DELETE /directors/{id} - Удаление режиссёра
}
