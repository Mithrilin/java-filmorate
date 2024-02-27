package ru.yandex.practicum.filmorate.dto.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarksParams {
    private Integer userId;
    private Integer filmId;
    private Integer mark;
}
