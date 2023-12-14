package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private static final int MAX_DESCRIPTION_SIZE = 200;
    private Integer id;
    @NotBlank
    @NonNull
    private final String name;
    @Size(max = MAX_DESCRIPTION_SIZE)
    private final String description;
    private final LocalDate releaseDate;
    @Positive
    private final Integer duration;
    private final Set<Integer> likes = new HashSet<>();

    public Film(String name, String description, LocalDate releaseDate, Integer duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
