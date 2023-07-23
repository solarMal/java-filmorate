package ru.yandex.practicum.filmorate.storage;

import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
     ResponseEntity<Film> createFilm(Film film);

     ResponseEntity<Film> updateFilm(Film updatedFilm);

     ResponseEntity<List<Film>> getAllFilms();

     Film getFilmById(Long filmId);

     List<Film> getFilms();

}
