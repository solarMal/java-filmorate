package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Component
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review createReview(Review review) {
        String sql = "INSERT INTO reviews (content, positive, user_id, film_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.isPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный id для Review");
        }

        review.setReviewId(keyHolder.getKey().longValue());
        review.setUseful(0);

        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ? WHERE id = ?";

        int rows = jdbcTemplate.update(
                sql,
                review.getContent(),
                review.getReviewId()
        );

        if (rows == 0) {
            throw new ReviewNotFoundException("отзыв с id " + review.getReviewId() + " не найден");
        }

        return review;
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        String sql = "SELECT review_id, content, user_id, film_id, useful FROM reviews WHERE id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, reviewRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("отзыв с id " + id + " не найден");
        }
    }

    @Override
    public void deleteReviewById(long id) {
        String sql = "DELETE FROM reviews WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteAllReviews() {
        String sql = "DELETE FROM reviews";

        jdbcTemplate.update(sql);
    }

    @Override
    public List<Review> getAllReviewsByFilmId(Long filmId, Integer count) {
        if (count == null) {
            count = 10;
        }

        String sql;

        if (filmId != null) {
            sql = "SELECT * FROM reviews " +
                    "WHERE film_id = ? " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
        } else {
            sql = "SELECT * FROM reviews " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sql, reviewRowMapper, count);
        }
    }

    @Override
    public void addLikeByUserId(long reviewId, long userId) {

        Boolean oldReaction = null;

        try {
            String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
            oldReaction = jdbcTemplate.queryForObject(checkSql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException ignored) {

        }

        String upsertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true) " +
                "ON CONFLICT (review_id, user_id) DO UPDATE SET is_like = true";

        jdbcTemplate.update(upsertSql, reviewId, userId);

        int delta = 0;

        if (oldReaction == null) {
            delta = 1;
        } else if (!oldReaction) {
            delta = 2;
        }

        String updateUseful = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(updateUseful, delta, reviewId);
    }

    @Override
    public void addDislikeByUserId(long reviewId, long userId) {

        Boolean oldReaction = null;

        try {
            String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
            oldReaction = jdbcTemplate.queryForObject(checkSql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException ignored) {

        }

        String upsertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false) " +
                "ON CONFLICT (review_id, user_id) DO UPDATE SET is_like = false";

        jdbcTemplate.update(upsertSql, reviewId, userId);

        int delta = 0;

        if (oldReaction == null) {
            delta = -1;
        } else if (oldReaction) {
            delta = -2;
        }

        String updateSql = "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";
        jdbcTemplate.update(updateSql, delta, reviewId);
    }

    @Override
    public void removeLike(long reviewId, long userId) {

        Boolean oldReaction = jdbcTemplate.queryForObject(
                "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?",
                Boolean.class,
                reviewId,
                userId
        );

        if (oldReaction == null || !oldReaction) {
            return;
        }

        jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                reviewId,
                userId
        );

        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?",
                reviewId
        );
    }

    @Override
    public void removeDislike(long reviewId, long userId) {

        Boolean oldReaction = jdbcTemplate.queryForObject(
                "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?",
                Boolean.class,
                reviewId,
                userId
        );

        if (oldReaction == null || oldReaction) {
            return;
        }

        jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?",
                reviewId,
                userId
        );

        jdbcTemplate.update(
                "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?",
                reviewId
        );
    }


    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> new Review(
            rs.getLong("review_id"),
            rs.getString("content"),
            rs.getBoolean("positive"),
            rs.getLong("user_id"),
            rs.getLong("film_id"),
            rs.getInt("useful")
    );
}
