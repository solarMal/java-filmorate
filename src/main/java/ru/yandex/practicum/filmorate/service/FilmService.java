package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    UserService userService;
    public FilmStorage filmStorage;

    @Autowired
    public FilmService(UserService userService, FilmStorage filmStorage) {
        this.userService = userService;
        this.filmStorage = filmStorage;
    }

    public ResponseEntity<?> createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public ResponseEntity<?> updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public ResponseEntity<?> getAllFilms() {
        return filmStorage.getAllFilms();
    }


    public ResponseEntity<?> addFilmLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById((int) filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            return ResponseEntity.notFound().build();
        }

        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        if (film.getFilmLikeByUserId().contains(userId)) {
            return ResponseEntity.badRequest().body("User has already liked this film.");
        }

        film.getFilmLikeByUserId().add(userId);

        log.info("Film like added. Film ID: {}, User ID: {}", filmId, userId);

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> deleteFilmLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById((int) filmId);
        User user = userService.getUserById(userId);

        if (film == null) {
            return ResponseEntity.notFound().build();
        }

        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        film.getFilmLikeByUserId().remove(userId);

        log.info("Film like removed. Film ID: {}, User ID: {}", filmId, userId);

        return ResponseEntity.ok().build();
    }


    public List<Film> getTop10Films() {
        List<Film> films = filmStorage.getFilms();

        films.sort((film1, film2) -> Integer.compare(film2.getFilmLikeByUserId().size(),
                film1.getFilmLikeByUserId().size()));

        return films.stream().limit(10).collect(Collectors.toList());
    }
}