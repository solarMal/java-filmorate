package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.createReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @GetMapping()
    public List<Review> getAllReviewsByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") Integer count) {

        return reviewService.getAllReviewsByFilmId(filmId, count);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable long id) {
        return reviewService.getReviewById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteReviewById(@PathVariable long id) {
        reviewService.deleteReviewById(id);
    }

    @DeleteMapping
    public void deleteAllReviews() {
        reviewService.deleteAllReviews();
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeByUserId(@PathVariable long id,
                                @PathVariable long userId) {
        reviewService.addLikeByUserId(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeByUserId(@PathVariable long id,
                                   @PathVariable long userId) {
        reviewService.addDislikeByUserId(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id,
                           @PathVariable long userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable long id,
                              @PathVariable long userId) {
        reviewService.removeDislike(id, userId);
    }
}
