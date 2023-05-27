package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validated.FilmValidated;

import java.util.ArrayList;
import java.util.List;

@Component
@Validated
public class InMemoryFilmStorage implements FilmStorage {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private int nextFilmId = 1;
    FilmValidated filmValidated = new FilmValidated();
    public List<Film> films = new ArrayList<>();

    @Override
    public ResponseEntity<?> createFilm(Film film) {
        try {
            filmValidated.validateAll(film);
            film.setId(nextFilmId++);
            films.add(film);
            log.info("информация о созданных фильмах: {}", films);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании фильма: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(film);
        }
        return ResponseEntity.ok(film);
    }

    @Override
    public ResponseEntity<?> updateFilm(Film updatedFilm) {
        try {
            int id = updatedFilm.getId();
            filmValidated.validateAll(updatedFilm);
            if (films.size() == 0) {
                films.add(updatedFilm);
            } else {
                boolean filmUpdated = false;
                for (int i = 0; i < films.size(); i++) {
                    Film film = films.get(i);
                    if (film.getId() == id) {
                        film.setName(updatedFilm.getName());
                        film.setDescription(updatedFilm.getDescription());
                        film.setReleaseDate(updatedFilm.getReleaseDate());
                        film.setDuration(updatedFilm.getDuration());
                        log.info("информация о обновлённых фильмах: {}", films);
                        filmUpdated = true;
                        break;
                    }
                }
                if (!filmUpdated) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(updatedFilm);
                }
            }
            return ResponseEntity.ok(updatedFilm);
        } catch (ValidationException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("колличество фильмов в текущий момент: {}", films.size());
        return new ResponseEntity<>(films, HttpStatus.OK);
    }

    @Override
    public Film getFilmById(int filmId) {
        if (films != null) {
            for (Film film : films) {
                if (film.getId() == filmId) {
                    return film;
                }
            }
        }
        return null;
    }

    @Override
    public List<Film> getFilms() {
        return films;
    }
}
