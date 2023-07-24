package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.daoimpl.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.FilmGenre;

import java.util.List;

@RestController
public class GenreController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    FilmDbStorage filmDbStorage;

    public GenreController(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    @GetMapping("/genres")
    public ResponseEntity<List<FilmGenre>> getAllGenres() {
        try {
            List<FilmGenre> genres = filmDbStorage.getAllGenresFromDatabase();
            return ResponseEntity.ok(genres);
        } catch (Exception e) {
            log.error("Ошибка при получении списка жанров: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/genres/{id}")
    public ResponseEntity<FilmGenre> getGenreById(@PathVariable int id) {
        try {
            FilmGenre genre = filmDbStorage.getGenreFromDatabaseById(id);
            if (genre == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(genre);
        } catch (Exception e) {
            log.error("Ошибка при получении жанра с ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
