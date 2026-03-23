package ru.yandex.practicum.filmorate.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "reviewId")
@ToString
@Builder
public class Review {
    private long reviewId;
    private String content;
    private boolean positive;
    private long userId;
    private long filmId;
    private int useful;

    public Review(String content, boolean positive, long userId, long filmId, int useful) {
        this.content = content;
        this.positive = positive;
        this.userId = userId;
        this.filmId = filmId;
        this.useful = useful;
    }
}
