package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate INITIAL_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private final Map<Integer, Film> films = new HashMap<>();
    private int generatedId = 1;

    @GetMapping
    public List<Film> findAll() {
        log.info("Текущее количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        if (film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            log.error("Фильм не прошёл валидацию.");
            throw new ValidationException("Фильм не прошёл валидацию.");
        }
        film.setId(generatedId++);
        log.info("Добавлен новый фильм с ID = {}", film.getId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody @Valid Film film) {
        if (!films.containsKey(film.getId()) || film.getReleaseDate().isBefore(INITIAL_RELEASE_DATE)) {
            log.error("Фильм не прошёл валидацию.");
            throw new ValidationException("Фильм не прошёл валидацию.");
        }
        log.info("Фильм с ID {} обновлён.", film.getId());
        films.put(film.getId(), film);
        return film;
    }
}
