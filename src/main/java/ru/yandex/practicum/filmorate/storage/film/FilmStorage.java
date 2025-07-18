package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Set;

public interface FilmStorage {
    Film createFilm(Film film);
    Film updateFilm(Film film);
    Set<Film> getAllFilms();
    Film getFilmById(long id);
}
