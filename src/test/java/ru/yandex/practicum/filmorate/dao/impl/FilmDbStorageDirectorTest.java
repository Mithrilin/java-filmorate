package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageDirectorTest {

    private FilmDbStorage filmDbStorage;
    private UserDbStorage userDbStorage;

    private DirectorDbStorage directorDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private Film[] film = new Film[6];

    private User[] user = new User[6];

    private Director directorOne = new Director(1, "Director one");
    private Director directorTwo = new Director(2, "Director two");
    private Director directorThree = new Director(3, "Director three");

    private static final String YEAR = "year";
    private static final String LIKES = "likes";

    @BeforeEach
    void setUp() {
        filmDbStorage = new FilmDbStorage(jdbcTemplate);
        userDbStorage = new UserDbStorage(jdbcTemplate);
        directorDbStorage = new DirectorDbStorage(jdbcTemplate);


        directorDbStorage.addDirector(directorOne);
        directorDbStorage.addDirector(directorTwo);
        directorDbStorage.addDirector(directorThree);

        for (int i = 1; i < film.length + 1; i++) {
            int filmNam = i - 1;
            film[filmNam] = new Film("film" + i,
                    "Описание" + i,
                    LocalDate.of(2000 - i,1 + i,10 + i),
                    100 + i,
                    new Mpa(1,"G"),
                    directorThree);
            film[filmNam].setGenres(new ArrayList<>());
            film[filmNam].setId(i);
            if (filmNam < 3) {
                film[filmNam].setDirector(directorOne);
            } else if (filmNam > 2  && filmNam < 5) {
                film[filmNam].setDirector(directorTwo);
            }

            System.out.println(film[filmNam]);

            filmDbStorage.addFilm(film[filmNam]);
        }

        for (int i = 1; i < user.length + 1; i++) {
            int userNam = i - 1;
            user[userNam] = new User("email" + i,
                    "login" + i,
                    "name" + i,
                    LocalDate.of(1990, 1, 1));
            user[userNam].setId(i);
            System.out.println(user[userNam]);

            userDbStorage.addUser(user[userNam]);
        }

    }

    @Test
    @DisplayName("Получение фильмов определенного режиссера и отсортированные по году или количеству оценок")
    void testGetFilmsSortByDirectorId() {
        //сортировка по году
        assertIterableEquals(List.of(3, 2, 1),filmDbStorage.getFilmsSortByDirectorId(directorOne.getId(),YEAR)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает фильмы в последовательности 3, 2, 1");

        assertIterableEquals(List.of(5, 4),filmDbStorage.getFilmsSortByDirectorId(directorTwo.getId(),YEAR)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает фильмы в последовательности 5, 4");

        assertIterableEquals(List.of(6),filmDbStorage.getFilmsSortByDirectorId(directorThree.getId(),YEAR)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает фильм 6");

    // Оценки первому фильму
    filmDbStorage.addLike(1,user[0].getId());
    filmDbStorage.addLike(1,user[1].getId());
    filmDbStorage.addLike(1,user[2].getId());
    filmDbStorage.addLike(1,user[3].getId());
    // Оценки второму фильму
    filmDbStorage.addLike(2,user[0].getId());
    filmDbStorage.addLike(2,user[1].getId());
    // Оценки четвертому фильму
    filmDbStorage.addLike(4,user[3].getId());
    filmDbStorage.addLike(4,user[4].getId());

        //  System.out.println(directorDbStorage.getDirectors());


        assertIterableEquals(List.of(2, 1),filmDbStorage.getFilmsSortByDirectorId(directorOne.getId(),LIKES)
                .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает два фильма в последовательности 2, 1");

        assertIterableEquals(List.of(4),filmDbStorage.getFilmsSortByDirectorId(directorTwo.getId(),LIKES)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает оди фильм 4");

        //filmDbStorage.getFilmsSortByDirectorId(directorThree.getId(),LIKES).stream().forEach(System.out::println);

        assertIterableEquals(List.of(),filmDbStorage.getFilmsSortByDirectorId(directorThree.getId(),LIKES)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает ни одного фильма");

        // Удалить оценки у первого фильма
        filmDbStorage.deleteLike(1,user[0].getId());
        filmDbStorage.deleteLike(1,user[1].getId());

        // Оценки третьему фильму
        filmDbStorage.addLike(3,user[1].getId());
        filmDbStorage.addLike(3,user[2].getId());
        filmDbStorage.addLike(3,user[3].getId());

        assertIterableEquals(List.of(1, 2, 3),filmDbStorage.getFilmsSortByDirectorId(directorOne.getId(),LIKES)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает два фильма в последовательности 1, 2, 3");

        // Удалить оценки у второго фильма
        filmDbStorage.deleteLike(2,user[0].getId());

        // filmDbStorage.getFilmsSortByDirectorId(directorOne.getId(),LIKES).stream().forEach(System.out::println);

        assertIterableEquals(List.of(2, 1, 3),filmDbStorage.getFilmsSortByDirectorId(directorOne.getId(),LIKES)
                        .stream().map(f->f.getId()).collect(Collectors.toList()),
                "возвращает три фильма в последовательности 2, 1, 3");
    }

}