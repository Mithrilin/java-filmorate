package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationsParams {
    private Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff;
    private Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark;
    private Map<Integer, Integer> userIdToMatch;
    private int requesterId;
}
