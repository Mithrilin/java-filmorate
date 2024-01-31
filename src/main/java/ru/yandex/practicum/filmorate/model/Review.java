package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank
    private String content;

    @NotNull
    private boolean isPositive;

    @NotNull
    private int userId;

    @NotNull
    private int filmId;

    private Integer useful;

    public Review() {}

    public Review(
            int reviewId, String content, boolean isPositive, int userId, int filmId, int useful
    ) {
        this.reviewId = reviewId;
        this.content = content;
        this.isPositive = isPositive;
        this.userId = userId;
        this.filmId = filmId;
        this.useful = useful;
    }

    @JsonProperty("isPositive")
    public boolean getIsPositive() {
        return isPositive;
    }
}
