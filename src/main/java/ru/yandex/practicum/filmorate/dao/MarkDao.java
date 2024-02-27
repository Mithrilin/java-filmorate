package ru.yandex.practicum.filmorate.dao;

import java.util.HashMap;
import java.util.Map;

public interface MarkDao {
    Map<Integer, HashMap<Integer, Integer>> getUserIdToFilmIdWithMark(int requesterId);
}