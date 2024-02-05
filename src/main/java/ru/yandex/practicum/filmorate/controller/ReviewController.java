package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/reviews")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Review getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Review> getAllReviewsByFilmId(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return reviewService.getAllReviewsByFilmId(filmId, count);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.likeReview(id, userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.dislikeReview(id, userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLikeFromReview(id, userId);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeFromReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeDislikeFromReview(id, userId);
    }
}
