package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    FilmService filmService = new FilmService();
    private final List<Film> films = new ArrayList<>();
    private int nextFilmId = 1;

    @PostMapping
    public ResponseEntity<?> createFilm(@RequestBody Film film) {
        try {
            if (film.getId() == 0){
                film.setId(nextFilmId++);
            }else {
                nextFilmId++;
            }
            filmService.validateAll(film);
            films.add(film);
            log.info("информация о созданных фильмах: {}", films);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании фильма: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(film);
        }
        return ResponseEntity.ok(film);
    }

//    @PutMapping()
//    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film updatedFilm) {
//        try {
//            int id = updatedFilm.getId();
//            filmService.validateAll(updatedFilm);
//            for (int i = 0; i < films.size(); i++) {
//                Film film = films.get(i);
//                if (film.getId() == id) {
//                    updatedFilm.setId(id);
//                    films.set(i, updatedFilm);
//                    log.info("информация о обновлённых фильмах: {}", films);
//                    return ResponseEntity.ok(updatedFilm);
//                }
//            }
//            return ResponseEntity.notFound().build();
//        } catch (ValidationException exception) {
//            return ResponseEntity.badRequest().body(updatedFilm);
//        }
//    }

    @PutMapping()
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film updatedFilm) {
        try {
            int id = updatedFilm.getId();
            filmService.validateAll(updatedFilm);
            if (films.size() == 0) {
                films.add(updatedFilm);
            } else {
                for (int i = 0; i < films.size(); i++) {
                    Film film = films.get(i);
                    if (film.getId() == id) {
                        // Обновляем только необходимые поля
                        film.setName(updatedFilm.getName());
                        film.setDescription(updatedFilm.getDescription());
                        film.setReleaseDate(updatedFilm.getReleaseDate());
                        film.setDuration(updatedFilm.getDuration());
                        log.info("информация о обновлённых фильмах: {}", films);
                        return ResponseEntity.ok(film);
                    }
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