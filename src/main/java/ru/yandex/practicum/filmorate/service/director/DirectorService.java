package ru.yandex.practicum.filmorate.service.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;


public interface DirectorService {
    List<Director> getDirectors (); //GET /directors - Список всех режиссёров

    List<Director> getDirectorById(Integer id); //GET /directors/{id}- Получение режиссёра по id

    Director addDirector(Director director); //POST /directors - Создание режиссёра

    Director updateDirector(Director director); //PUT /directors - Изменение режиссёра

    void deleteDirector(Integer id); //DELETE /directors/{id} - Удаление режиссёра
}
