package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.daoimpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.FilmMPA;

import java.util.List;

@RestController
public class MpaController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    FilmDbStorage filmDbStorage;

    public MpaController(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    @GetMapping("/mpa")
    public ResponseEntity<List<FilmMPA>> getAllFilmMPAs() {
        try {
            List<FilmMPA> mpaList = filmDbStorage.getAllFilmMPAsFromDatabase();
            return ResponseEntity.ok(mpaList);
        } catch (Exception e) {
            log.error("Ошибка при получении списка рейтингов фильмов: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mpa/{id}")
    public ResponseEntity<FilmMPA> getFilmMPAById(@PathVariable int id) {
        try {
            FilmMPA mpa = filmDbStorage.getFilmMPAFromDatabaseById(id);
            if (mpa == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mpa);
        } catch (Exception e) {
            log.error("Ошибка при получении рейтинга фильма с ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}