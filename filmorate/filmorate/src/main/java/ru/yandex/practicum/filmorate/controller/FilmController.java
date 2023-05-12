package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    FilmService filmService = new FilmService();
    private final List<Film> films = new ArrayList<>();
    private int nextFilmId = 1;

    @PostMapping
    public ResponseEntity<?> createFilm(@RequestBody Film film) {
        film.setId(nextFilmId++);
        try {
            filmService.validateAll(film);
            films.add(film);
            log.info("информация о созданных фильмах: {}", films);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании фильма: {}", exception.getMessage());
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
        return ResponseEntity.ok(film);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFilm(@PathVariable int id, @RequestBody Film updatedFilm) {
        try {
            filmService.validateAll(updatedFilm);
            for (int i = 0; i < films.size(); i++) {
                Film film = films.get(i);
                if (film.getId() == id) {
                    updatedFilm.setId(id);
                    films.set(i, updatedFilm);
                    log.info("информация о обновлённых фильмах: {}", films);
                    return ResponseEntity.ok(updatedFilm);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (ValidationException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("колличество фильмов в текущий момент: {}", films.size());
        return new ResponseEntity<>(films, HttpStatus.OK);
    }
}