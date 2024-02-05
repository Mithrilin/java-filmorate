package ru.yandex.practicum.filmorate.service.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {
    Review getReviewById(int id);

    List<Review> getAllReviewsByFilmId(Integer filmId, int count);

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int id);

    void likeReview(int id, int userId);

    void dislikeReview(int id, int userId);

    void removeLikeFromReview(int id, int userId);

    void removeDislikeFromReview(int id, int userId);
}
