package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    Optional<Review> getReviewById(int id);

    List<Review> getAllReviewsByFilmId(int filmId, int count);

    List<Review> getAllReviews(int count);

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int id);

    void likeReview(int id, int userId);

    void dislikeReview(int id, int userId);

    void removeLikeFromReview(int id, int userId);

    void removeDislikeFromReview(int id, int userId);
}
