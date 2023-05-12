package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(FilmService.class);

    public void filmNameCannotBeEmptyValidation(Film film) {
        if (film.getName().isEmpty()) {
            log.warn("У фильма должно быть название");
            throw new ValidationException("У фильма должно быть название");
        }
    }

    public void maxLengthDescriptionValidation(Film film) {
        if (film.getDescription().length() > 200) {
            log.warn("Максимальная длинна описания должна быть меньше 200 символов");
            throw new ValidationException("Максимальная длинна описания должна быть меньше 200 символов");
        }
    }

    public void releaseDateValidation(Film film) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        LocalDate actualReleaseDate = film.getReleaseDate();
        if (actualReleaseDate.isBefore(minReleaseDate)) {
            log.warn("Дата релиза должна быть не раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    public void durationValidation(Film film) {
        if (film.getDuration() <= 0) {
            log.warn("Продолжительность фильма должна быть положительной");
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }
    }

    public void validateAll(Film film) {
        filmNameCannotBeEmptyValidation(film);
        maxLengthDescriptionValidation(film);
        releaseDateValidation(film);
        durationValidation(film);
    }
}
