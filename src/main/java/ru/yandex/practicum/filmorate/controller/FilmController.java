package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/film")
@Validated
public class FilmController {
    FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<?> createFilm(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film updatedFilm) {
        return filmService.updateFilm(updatedFilm);
    }

    @GetMapping
    public ResponseEntity<?> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable("id") Long id) {
        Film film = filmService.filmStorage.getFilmById(id);
        if (film == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addFilmLike(@PathVariable("id") Long id, @PathVariable("userId") long userId) {
        return filmService.addFilmLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> deleteFilmLike(@PathVariable("id") Long id, @PathVariable("userId") long userId) {
        return filmService.deleteFilmLike(id, userId);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getTopFilmsByLikes(@RequestParam(value = "count",defaultValue = "10") int count) {
        List<Film> films = filmService.getTop10Films();

        List<Film> topFilms = films.stream().limit(count).collect(Collectors.toList());

        return ResponseEntity.ok(topFilms);
    }
}