package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film createFilm(@RequestBody @Valid Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody @Valid Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @GetMapping
    public List<Film> findAllFilms() {
        return filmService.getAllFilms();
    }

    @PutMapping("/{id}/mark/{userId}")
    public void addMark(@PathVariable int id,
                        @PathVariable int userId,
                        @RequestParam int mark) {
        filmService.addMark(id, userId, mark);
    }

    @DeleteMapping("/{id}/mark/{userId}")
    public void deleteMark(@PathVariable int id,
                           @PathVariable int userId) {
        filmService.deleteMark(id, userId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false) @Min(0) Integer count,
                                      @RequestParam(defaultValue = "0") Integer genreId,
                                      @RequestParam(required = false) @Min(1895) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirectorId(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmService.getFilmsByDirectorId(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> getFilmsBySearch(@RequestParam String query, @RequestParam List<String> by) {
        return filmService.getFilmsBySearch(query, by);
    }
}
