package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> {
        return new Review(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getBoolean("isPositive"),
                rs.getInt("user_id"),
                rs.getInt("film_id"),
                rs.getInt("useful")
        );
    };

    private final RowMapper<Boolean> rateMapper = (rs, rowNum) ->
            rs.getBoolean("isPositive");

    @Override
    public Optional<Review> getReviewById(int id) {
        String sqlQuery = "SELECT * FROM reviews WHERE id = ?;";

        return jdbcTemplate.query(sqlQuery, reviewRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        String sqlQuery = "" +
                "SELECT * " +
                "FROM reviews " +
                "WHERE film_id = ? " +
                "ORDER BY useful DESC " +
                "LIMIT ?;";

        return jdbcTemplate.query(sqlQuery, reviewRowMapper, filmId, count);
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sqlQuery = "" +
                "SELECT * " +
                "FROM reviews " +
                "ORDER BY useful DESC " +
                "LIMIT ?;";

        return jdbcTemplate.query(sqlQuery, reviewRowMapper, count);
    }

    @Override
    public Review createReview(Review review) {
        String sqlInsertReview =
                "INSERT INTO reviews (content, isPositive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sqlInsertReview, new String[]{"ID"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, review.getUseful());

            return ps;
        }, keyHolder);

        int reviewId = keyHolder.getKey().intValue();

        return getReviewById(reviewId).get();
    }

    @Override
    public Review updateReview(Review review) {
        String sqlUpdateReview =
                "UPDATE reviews " +
                "SET " +
                        "content = ?, " +
                        "isPositive = ? " +
                "WHERE id = ?;";

        jdbcTemplate.update(
                sqlUpdateReview,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        return getReviewById(review.getReviewId()).get();
    }

    @Override
    public void deleteReview(int id) {
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?;", id);
    }

    @Override
    public void likeReview(int id, int userId) {
        String sqlInsertReviewsLikes =
                "INSERT INTO users_reviews_rates (user_id, review_id, isPositive) " +
                "VALUES (?, ?, true);";

        jdbcTemplate.update(sqlInsertReviewsLikes, userId, id);

        String sqlUpdateReview =
                "UPDATE reviews " +
                "SET useful = useful + 1 " +
                "WHERE id = ?;";

        jdbcTemplate.update(sqlUpdateReview, id);
    }

    @Override
    public void dislikeReview(int id, int userId) {
        String sqlInsertReviewsLikes =
                "INSERT INTO users_reviews_rates (user_id, review_id, isPositive) " +
                "VALUES (?, ?, false);";

        jdbcTemplate.update(sqlInsertReviewsLikes, userId, id);

        String sqlUpdateReview =
                "UPDATE reviews " +
                "SET useful = useful - 1 " +
                "WHERE id = ?;";

        jdbcTemplate.update(sqlUpdateReview, id);
    }

    @Override
    public void removeLikeFromReview(int id, int userId) {
        String sqlSelectReviewRate =
                "SELECT isPositive " +
                "FROM users_reviews_rates " +
                "WHERE user_id = ?;";

        Consumer<Boolean> performIfPresent = (isPositive) -> {
            String sqlDeleteRate = "DELETE FROM users_reviews_rates WHERE user_id = ?;";
            String sqlUpdateReview =
                    "UPDATE reviews " +
                    "SET useful = useful" + (isPositive ? " - 1 " : " + 1 ") +
                    "WHERE id = ?;";

            jdbcTemplate.update(sqlDeleteRate, userId);
            jdbcTemplate.update(sqlUpdateReview, id);
        };

        jdbcTemplate.query(sqlSelectReviewRate, rateMapper, userId)
                .stream()
                .findFirst()
                .ifPresent(performIfPresent);
    }

    @Override
    public void removeDislikeFromReview(int id, int userId) {
        String sqlSelectReviewDislike =
                "SELECT isPositive " +
                "FROM users_reviews_rates " +
                "WHERE user_id = ? AND isPositive = false;";

        Consumer<Boolean> performIfPresent = (value) -> {
            String sqlUpdateReview = "UPDATE reviews SET useful = useful + 1 WHERE id = ?;";
            String sqlDeleteDislike = "DELETE FROM users_reviews_rates WHERE user_id = ?;";

            jdbcTemplate.update(sqlDeleteDislike, userId);
            jdbcTemplate.update(sqlUpdateReview, id);
        };

        jdbcTemplate.query(sqlSelectReviewDislike, rateMapper, userId)
                .stream()
                .findFirst()
                .ifPresent(performIfPresent);
    }
}
