package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Director {
    private int id;
    private String name;

    public Director(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Director () {

    }

}
