package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    ReviewStorage reviewStorage;
    FilmStorage filmStorage;
    UserStorage userStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage) {
        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Review createReview(Review review) {
        validateReview(review);
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public Review getReviewById(long id) {
        return reviewStorage.getReviewById(id)
                .orElseThrow(() -> new ReviewNotFoundException("отзыв с id " + id + " не найден"));
    }

    public void deleteReviewById(long id) {
        reviewStorage.deleteReviewById(id);
        log.info("отзыв {} удалён", id);
    }

    public void deleteAllReviews() {
        reviewStorage.deleteAllReviews();
        log.info("все отзывы удалены");
    }

    public List<Review> getAllReviewsByFilmId(Long filmId, Integer count) {
        return reviewStorage.getAllReviewsByFilmId(filmId, count);
    }

    public void addLikeByUserId(long id, long userId) {
        reviewStorage.addLikeByUserId(id, userId);
    }

    public void addDislikeByUserId(long id, long userId) {
        reviewStorage.addDislikeByUserId(id, userId);
    }

    public void removeLike(long id, long userId) {
        reviewStorage.removeLike(id, userId);
    }

    public void removeDislike(long id, long userId) {
        reviewStorage.removeDislike(id, userId);
    }

    void validateReview(Review review) {
        if (review == null) {
            throw new ReviewNotFoundException("отзыв не существует");
        }

        User user = userStorage.getUserById(review.getUserId());
        Film film = filmStorage.getFilmById(review.getFilmId());

        if (user == null) {
            throw new UserNotFoundException("пользователь не существует");
        }

        if (film == null) {
            throw new FilmNotFoundException("Фильм не существует");
        }

    }
}
