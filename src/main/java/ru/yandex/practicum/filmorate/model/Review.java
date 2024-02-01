package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class Review {
    private Integer reviewId;

    @NotBlank
    @Size(max = 255)
    private String content;

    @NotNull
    private Boolean isPositive;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer filmId;

    private Integer useful;

    public Review() {
        this.useful = 0;
    }

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
