package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.daoimpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmDbStorageController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    FilmDbStorage filmDbStorage;

    public FilmDbStorageController(@Qualifier("FilmDbStorage") FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    @PostMapping
    public ResponseEntity<?> createFilm(@RequestBody Film film) {
        return filmDbStorage.createFilm(film);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@RequestBody Film updatedFilm) {
        return filmDbStorage.updateFilm(updatedFilm);
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return filmDbStorage.getAllFilms();
    }

    @GetMapping("/{filmId}")
    public ResponseEntity<Film> getFilmById(@PathVariable int filmId) {
        Film film = filmDbStorage.getFilmById(filmId);
        if (film != null) {
            return ResponseEntity.ok(film);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<?> addFilmLike(@PathVariable long filmId, @PathVariable long userId) {
        return filmDbStorage.addFilmLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<?> removeFilmLike(@PathVariable long filmId, @PathVariable long userId) {
        return filmDbStorage.removeFilmLike(filmId, userId);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@RequestParam(value = "count", required = false,
            defaultValue = "10") int count) {
        List<Film> films = filmDbStorage.getPopularFilms(count);
        if (films.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(films);
    }
}
