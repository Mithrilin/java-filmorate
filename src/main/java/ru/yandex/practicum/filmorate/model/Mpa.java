package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Mpa {
    private Integer id;
    @NotBlank
    @NotNull
    private String name;

    public Mpa(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Mpa() {
    }
}
