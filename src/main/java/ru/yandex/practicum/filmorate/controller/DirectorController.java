package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.director.DirectorService;

import javax.validation.Valid;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    //GET /directors - Список всех режиссёров
    @GetMapping
    public List<Director> getDirectors() {
        return directorService.getDirectors();
    }

    //GET /directors/{id}- Получение режиссёра по id
    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    //POST /directors - Создание режиссёра
    @PostMapping
    public Director addDirector(@RequestBody @Valid Director director) {
        return directorService.addDirector(director);
    }

    //PUT /directors - Изменение режиссёра
    @PutMapping
    public Director updateDirector(@RequestBody @Valid Director director) {
        return directorService.updateDirector(director);
    }

    //DELETE /directors/{id} - Удаление режиссёра
    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        directorService.deleteDirector(id);
    }
}
