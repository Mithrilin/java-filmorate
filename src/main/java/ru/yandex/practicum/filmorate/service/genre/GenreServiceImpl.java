package ru.yandex.practicum.filmorate.service.genre;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class GenreServiceImpl implements GenreService{
    private final GenreDao genreDao;

    @Override
    public Genre getGenreById(int id) {
        Genre genre = isIdValid(id);
        log.info("Жанр с id {} возвращён.", genre.getId());
        return genre;
    }

    @Override
    public List<Genre> getAllGenres() {
        return null;
    }

    private Genre isIdValid(int id) {
        Optional<Genre> genre = genreDao.getGenreById(id);
        if (genre.isEmpty()) {
            throw new GenreNotFoundException("Жанр с id " + id + " не найден.");
        }
        return genre.get();
    }
}
