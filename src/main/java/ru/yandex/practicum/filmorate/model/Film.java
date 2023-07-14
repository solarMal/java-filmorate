package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be a positive value")
    private int duration;

    @ManyToMany
    @JoinTable(name = "FilmGenre", joinColumns = @JoinColumn(name = "film_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<FilmGenre> genres;

    @ManyToOne
    @JoinColumn(name = "film_mpa_id")
    private FilmMPA mpa;

    @ElementCollection
    @CollectionTable(name = "filmLike", joinColumns = @JoinColumn(name = "filmId"))
    @Column(name = "userId")
    private Set<Long> filmLikeByUserId = new HashSet<>();

    public Film() {
    }

    public Film(int id, @NotBlank(message = "Name is required") String name
            , @NotNull(message = "Release date is required") LocalDate releaseDate
            , String description
            , @Positive(message = "Duration must be a positive value") int duration
            , FilmMPA mpa
            , List<FilmGenre> genre
            , Set<Long> filmLikeByUserId) {
        this.id = id;
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.duration = duration;
        this.mpa = mpa;
        this.genres = genre;
        this.filmLikeByUserId = filmLikeByUserId;
    }
}
