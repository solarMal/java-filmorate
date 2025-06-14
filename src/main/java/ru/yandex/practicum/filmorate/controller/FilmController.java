package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    int dynamicId = 1;
    Map<Integer, Film> films = new HashMap<>();

    @PostMapping
    public Film createFilm(@RequestBody Film film) throws ValidateException {
        filmValidate(film);
        film.setId(dynamicId++);
        films.put(film.getId(), film);
        log.info("фильм успешно добавлен {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) throws ValidateException {
        filmValidate(film);

        if (!films.containsKey(film.getId())) {
            throw new ValidateException("ошибка обновления фильма");
        }

        films.put(film.getId(), film);
        log.info("фильм успешно обновлён");
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() throws ValidateException {
        if (films.isEmpty()) {
            throw new ValidateException("нет добавленных фильмов");
        }
        return new ArrayList<>(films.values());
    }


    public void filmValidate(Film film) throws ValidateException {
        if (film == null) {
            throw new ValidateException("Фильм не может быть null");
        }

        if (film.getName().isBlank()) {
            throw new ValidateException("название фильма не может быть пустым или состоять из пробелов");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidateException("максимальная длинна фильма не должна превышать 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidateException("дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidateException("продолжительность фильма должна быть положительной");
        }
    }
}
