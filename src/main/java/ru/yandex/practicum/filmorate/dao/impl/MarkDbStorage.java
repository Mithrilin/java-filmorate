package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.MarkDao;
import ru.yandex.practicum.filmorate.model.Mark;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("markDbStorage")
public class MarkDbStorage implements MarkDao {
    private static final String USERS_MARKS_SQL =
            "SELECT * " +
            "FROM marks AS m1 " +
            "WHERE m1.user_id IN (SELECT m2.user_id " +
                                  "FROM marks AS m2 " +
                                  "WHERE m2.film_id IN (SELECT m3.film_id " +
                                                        "FROM marks AS m3 " +
                                                        "WHERE m3.user_id = ?))";
    private final JdbcTemplate jdbcTemplate;

    public MarkDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, HashMap<Integer, Integer>> getUserIdToFilmIdWithMark(int requesterId) {
        List<Mark> markList = jdbcTemplate.query(USERS_MARKS_SQL, marksRowMapper(), requesterId);
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark = new HashMap<>();
        for (Mark mark : markList) {
            int userId = mark.getUserId();
            int filmId = mark.getFilmId();
            int markCount = mark.getMark();
            if (!userIdToFilmIdWithMark.containsKey(userId)) {
                userIdToFilmIdWithMark.put(userId, new HashMap<>());
            }
            userIdToFilmIdWithMark.get(userId).put(filmId, markCount);
        }
        return userIdToFilmIdWithMark;
    }

    private RowMapper<Mark> marksRowMapper() {
        return (rs, rowNum) -> new Mark(
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("mark")
        );
    }
}
