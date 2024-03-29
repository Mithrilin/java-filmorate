package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class Film {
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private Integer id;
    @NotBlank
    private final String name;
    @Size(max = MAX_DESCRIPTION_SIZE)
    private final String description;
    private final LocalDate releaseDate;
    @Positive
    private final Integer duration;
    private final Mpa mpa;
    private List<Genre> genres = new ArrayList<>();
    private Double rating;
    private List<Director> directors = new ArrayList<>();

    public Film(String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
        this.rating = 0.0;
    }
}
