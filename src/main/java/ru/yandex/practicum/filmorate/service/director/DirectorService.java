package ru.yandex.practicum.filmorate.service.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorService {
    List<Director> getDirectors (); //GET /directors - Список всех режиссёров

    Director getDirectorById(int id); //GET /directors/{id}- Получение режиссёра по id

    Director createDirector(Director director); //POST /directors - Создание режиссёра

    Director upDirector(Director director); //PUT /directors - Изменение режиссёра

    void deleteDirector(int id);//DELETE /directors/{id} - Удаление режиссёра
}
