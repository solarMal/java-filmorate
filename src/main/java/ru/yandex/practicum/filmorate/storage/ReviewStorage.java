package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review review);

    Optional<Review> getReviewById(long id);

    void deleteReviewById(long id);

    void deleteAllReviews();

    List<Review> getAllReviewsByFilmId(Long filmId, Integer count);

    void addLikeByUserId(long id, long userId);

    void addDislikeByUserId(long id, long userId);

    void removeLike(long id, long userId);

    void removeDislike(long id, long userId);
}
