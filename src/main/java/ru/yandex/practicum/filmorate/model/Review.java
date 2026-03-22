package ru.yandex.practicum.filmorate.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class Review {
    private long id;
    private long userId;
    private long filmId;
    private String reviewText;

    public Review(long userId, long filmId, String reviewText) {
        this.userId = userId;
        this.filmId = filmId;
        this.reviewText = reviewText;
    }

    public Review(long id, String reviewText) {
        this.id = id;
        this.reviewText = reviewText;
    }
}
