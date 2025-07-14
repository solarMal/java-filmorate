package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    int dynamicId = 1;
    Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film createFilm(Film film) {
        filmValidate(film);
        film.setId(dynamicId++);
        films.put(film.getId(), film);
        log.info("фильм успешно добавлен {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        filmValidate(film);

        if (!films.containsKey(film.getId())) {
            throw new ValidateException("ошибка обновления фильма");
        }

        films.put(film.getId(), film);
        log.info("фильм успешно обновлён");
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
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
