package ru.yandex.practicum.filmorate.service.genre;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class GenreServiceImpl implements GenreService{
    private final GenreDao genreDao;

    @Override
    public Genre getGenreById(int id) {
        return null;
    }

    @Override
    public List<Genre> getAllGenres() {
        return null;
    }
}
