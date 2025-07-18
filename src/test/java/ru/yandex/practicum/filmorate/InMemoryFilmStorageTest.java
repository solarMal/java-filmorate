package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;

public class InMemoryFilmStorageTest {
    InMemoryFilmStorage filmStorage;

    @BeforeEach
    void beforeEach() {
        filmStorage = new InMemoryFilmStorage();
    }

    @Test
    public void testValidFilm() {
        final Film film = Film.builder()
                .id(1)
                .name("Inception")
                .description("Great movie")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .build();

        assertDoesNotThrow(() -> filmStorage.filmValidate(film));
    }

    @Test
    public void testBlankName() {
        Film film = new Film(1, "   ", "Great movie", LocalDate.of(2010, 7, 16), 148);
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmStorage.filmValidate(film));
        assertEquals("название фильма не может быть пустым или состоять из пробелов", exception.getMessage());
    }

    @Test
    public void testTooLongDescription() {
        String longDescription = "a".repeat(201);
        Film film = new Film(1, "Inception", longDescription, LocalDate.of(2010, 7, 16), 148);
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmStorage.filmValidate(film));
        assertEquals("максимальная длинна фильма не должна превышать 200 символов", exception.getMessage());
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film(1, "Inception", "Great movie", LocalDate.of(2010, 7, 16), -100);
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmStorage.filmValidate(film));
        assertEquals("продолжительность фильма должна быть положительной", exception.getMessage());
    }

    @Test
    public void testTooEarlyDate() {
        Film film = new Film(1, "Inception", "Great movie", LocalDate.of(1800, 1, 1), 120);
        ValidateException exception = assertThrows(ValidateException.class,
                () -> filmStorage.filmValidate(film));
        assertEquals("дата релиза должна быть не раньше 28 декабря 1895 года", exception.getMessage());
    }
}
