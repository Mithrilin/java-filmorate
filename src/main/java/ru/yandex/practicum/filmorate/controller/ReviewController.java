package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

@RequestMapping("/reviews")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Review getReviewById(@PathVariable int id) {
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Review> getAllReviewsByFilmId(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(required = false, defaultValue = "10") int count
    ) {
        return List.of();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Review createReview(Review review) {
        return null;
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Review updateReview(Review review) {
        return null;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {

    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable int id, @PathVariable int userId) {

    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable int id, @PathVariable int userId) {

    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeFromReview(@PathVariable int id, @PathVariable int userId) {
        
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeFromReview(@PathVariable int id, @PathVariable int userId) {

    }
}
