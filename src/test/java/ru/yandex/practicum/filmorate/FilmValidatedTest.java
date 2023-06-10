package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validated.FilmValidated;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class FilmValidatedTest {
    private FilmValidated filmValidated;
    private Film film;

    @BeforeEach
    public void setup() {
        filmValidated = new FilmValidated();
        film = new Film();
    }

    @Test
    public void testFilmNameCannotBeEmptyValidation() {
        film.setName("");

        assertThrows(ValidationException.class, () -> {
            filmValidated.filmNameCannotBeEmptyValidation(film);
        });
    }

    @Test
    public void testMaxLengthDescriptionValidation() {
        film.setDescription("слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание ");

        assertThrows(ValidationException.class, () -> {
            filmValidated.maxLengthDescriptionValidation(film);
        });
    }

    @Test
    public void testReleaseDateValidation() {
        LocalDate releaseDate = LocalDate.of(1895, 1, 1);
        film.setReleaseDate(releaseDate);

        assertThrows(ValidationException.class, () -> {
            filmValidated.releaseDateValidation(film);
        });
    }

    @Test
    public void testDurationValidation() {
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> {
            filmValidated.durationValidation(film);
        });
    }

    @Test
    public void testValidateAll() {
        film.setName("");
        film.setDescription("слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание ");
        film.setReleaseDate(LocalDate.of(1895, 1, 1));
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> {
            filmValidated.validateAll(film);
        });
    }
}
