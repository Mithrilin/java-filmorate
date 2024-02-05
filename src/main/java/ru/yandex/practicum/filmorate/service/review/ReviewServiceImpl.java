package ru.yandex.practicum.filmorate.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.event.EventService;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDao reviewDao;
    private final FilmService filmService;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public Review getReviewById(int id) {
        Review review = reviewDao.getReviewById(id).orElseThrow(() ->
                new NotFoundException("Ревью не найдено"));
        log.info("Отзыв с ид {} возвращён.", review.getReviewId());
        return review;
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Integer filmId, int count) {
        List<Review> reviews = filmId != null
                ? reviewDao.getAllReviewsByFilmId(filmId, count)
                : reviewDao.getAllReviews(count);
        log.info("Список отзывов к фильму с ид {} возвращён.", filmId);
        return reviews;
    }

    @Override
    public Review createReview(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        Review addedReview = reviewDao.createReview(review);
        eventService.addEvent(new Event(addedReview.getUserId(), "REVIEW", "ADD", addedReview.getReviewId()));
        log.info("Отзыв к фильму с ид {} от пользователя с ид {} создан.", review.getFilmId(), review.getUserId());
        return addedReview;
    }

    @Override
    public Review updateReview(Review review) {
        reviewDao.getReviewById(review.getReviewId());
        userService.getUserById(review.getUserId());
        filmService.getFilmById(review.getFilmId());
        Review updatedReview = reviewDao.updateReview(review);
        eventService.addEvent(new Event(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId()));
        log.info("Отзыв к фильму с ид {} от пользователя с ид {} обновлён.", review.getFilmId(), review.getUserId());
        return reviewDao.updateReview(review);
    }

    @Override
    public void deleteReview(int id) {
        getReviewById(id);
        eventService.addEvent(new Event(getReviewById(id).getUserId(), "REVIEW", "REMOVE", id));
        reviewDao.deleteReview(id);
        log.info("Отзыв с ид {} удалён.", id);
    }

    @Override
    public void likeReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.likeReview(id, userId);
        log.info("Отзыв с ид {} понравился пользователю с ид {}.", id, userId);
    }

    @Override
    public void dislikeReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.dislikeReview(id, userId);
        log.info("Отзыв с ид {} НЕ понравился пользователю с ид {}.", id, userId);
    }

    @Override
    public void removeLikeFromReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.removeLikeFromReview(id, userId);
        log.info("Пользователю с ид {} удалил свой лайк к отзыву с ид {}.", userId, id);
    }

    @Override
    public void removeDislikeFromReview(int id, int userId) {
        getReviewById(id);
        userService.getUserById(userId);
        reviewDao.removeDislikeFromReview(id, userId);
        log.info("Пользователю с ид {} удалил свой дизлайк к отзыву с ид {}.", userId, id);
    }
}
