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
        String sql = "INSERT INTO reviews (user_id, film_id, review_text) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getUserId());
            ps.setLong(2, review.getFilmId());
            ps.setString(3, review.getReviewText());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный id для Review");
        }

        review.setId(keyHolder.getKey().longValue());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET review_text = ? WHERE id = ?";

        int rows = jdbcTemplate.update(
                sql,
                review.getReviewText(),
                review.getId()
        );

        if (rows == 0) {
            throw new ReviewNotFoundException("отзыв с id " + review.getId() + " не найден");
        }

        return review;
    }

    @Override
    public List<Review> getAllReviews() {
        String sql = "SELECT id, user_id, film_id, review_text FROM reviews";

        return jdbcTemplate.query(sql, reviewRowMapper);
    }

    @Override
    public Optional<Review> getReviewById(long id) {
        String sql = "SELECT id, user_id, film_id, review_text FROM reviews WHERE id = ?";

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


    private final RowMapper<Review> reviewRowMapper = (rs, rowNum) -> new Review(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getLong("film_id"),
            rs.getString("review_text")
    );
}
