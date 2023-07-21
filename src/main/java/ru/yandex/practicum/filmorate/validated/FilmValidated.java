package ru.yandex.practicum.filmorate.validated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.controller.ErrorHandler;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmValidated {
    private static final Logger log = LoggerFactory.getLogger(FilmValidated.class);

    public void filmNameCannotBeEmptyValidation(Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().trim().isEmpty()) {
            log.warn("У фильма должно быть название");
            throw new ValidationException("У фильма должно быть название");
        }
    }

    public void maxLengthDescriptionValidation(Film film) {
        String description = film.getDescription();
        if (description == null || description.trim().isEmpty()) {
            log.warn("Описание фильма не может быть пустым");
            throw new ValidationException("Описание фильма не может быть пустым");
        }

        if (description.length() > 200) {
            log.warn("Максимальная длинна описания должна быть меньше 200 символов");
            throw new ValidationException("Максимальная длинна описания должна быть меньше 200 символов");
        }
    }

    public void releaseDateValidation(Film film) {
        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        LocalDate actualReleaseDate = film.getReleaseDate();
        if (actualReleaseDate == null || actualReleaseDate.isBefore(minReleaseDate)) {
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


    public void validateAll(Film film) throws ValidationException {
        FilmValidated filmValidator = new FilmValidated();
        filmValidator.filmNameCannotBeEmptyValidation(film);
        filmValidator.maxLengthDescriptionValidation(film);
        filmValidator.releaseDateValidation(film);
        filmValidator.durationValidation(film);
    }
}
