package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    List<Review> getAllReviews();

    Optional<Review> getReviewById(long id);

    void deleteReviewById(long id);

    void deleteAllReviews();
}
