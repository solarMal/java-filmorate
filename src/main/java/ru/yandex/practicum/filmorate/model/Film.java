package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private int id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be a positive value")
    private int duration;

    FilmGenre filmGenre;

    FilmMPA filmMPA;

    Set<Long> filmLikeByUserId = new HashSet<>();

}
