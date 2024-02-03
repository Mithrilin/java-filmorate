package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;

    private final FilmService filmService;

    private final UserService userService;

    private final EventService eventService;

    @Override
    public Review getReviewById(int id) {
        return reviewDao.getReviewById(id).orElseThrow(() ->
                new NotFoundException("Ревью не найдено"));
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, int count) {
        return filmId != null
                ? reviewDao.getAllReviewsByFilmId(filmId, count)
                : reviewDao.getAllReviews(count);
    }

    @Override
    public Review createReview(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        Review addedReview = reviewDao.createReview(review);
        eventService.addEvent(new Event(addedReview.getUserId(), "REVIEW", "ADD", addedReview.getReviewId()));
        return addedReview;
    }

    @Override
    public Review updateReview(Review review) {
        reviewDao.getReviewById(review.getReviewId());
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        Review updatedReview = reviewDao.updateReview(review);
        eventService.addEvent(new Event(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId()));
        return reviewDao.updateReview(review);
    }

    @Override
    public void deleteReview(int id) {
        getReviewById(id);
        eventService.addEvent(new Event(getReviewById(id).getUserId(), "REVIEW", "REMOVE", id));
        reviewDao.deleteReview(id);
    }

    @Override
    public void likeReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.likeReview(id, userId);
    }

    @Override
    public void dislikeReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.dislikeReview(id, userId);
    }

    @Override
    public void removeLikeFromReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.removeLikeFromReview(id, userId);
    }

    @Override
    public void removeDislikeFromReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.removeDislikeFromReview(id, userId);
    }
}
