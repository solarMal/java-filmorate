package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class FilmServiceTest {
    private FilmService filmService;
    private Film film;

    @BeforeEach
    public void setup() {
        filmService = new FilmService();
        film = new Film();
    }

    @Test
    public void testFilmNameCannotBeEmptyValidation() {
        film.setName(""); // Empty film name

        assertThrows(ValidationException.class, () -> {
            filmService.filmNameCannotBeEmptyValidation(film);
        });
    }

    @Test
    public void testMaxLengthDescriptionValidation() {
        film.setDescription("слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание " +
                "слишком длинное описание слишком длинное описание слишком длинное описание ");

        assertThrows(ValidationException.class, () -> {
            filmService.maxLengthDescriptionValidation(film);
        });
    }

    @Test
    public void testReleaseDateValidation() {
        LocalDate releaseDate = LocalDate.of(1895, 1, 1);
        film.setReleaseDate(releaseDate);

        assertThrows(ValidationException.class, () -> {
            filmService.releaseDateValidation(film);
        });
    }

    @Test
    public void testDurationValidation() {
        film.setDuration(-1);

        assertThrows(ValidationException.class, () -> {
            filmService.durationValidation(film);
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
            filmService.validateAll(film);
        });
    }
}
