package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage
            , @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addLike(long id, long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);

        if (film.getLikes().contains(user.getId())) {
            throw new LikeFromUserException("пользователь с id " + user.getId() + " уже поставил лайк этому фильму");
        }

        filmStorage.addLike(id, userId);
        film.getLikes().add(user.getId());

        return filmStorage.getFilmById(id);
    }

    public Film removeLike(long id, long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);

        if (!film.getLikes().contains(user.getId())) {
            throw new LikeFromUserException("Пользователь с id " + user.getId() + " ещё не ставил лайк этому фильму");
        }

        filmStorage.removeLike(id, userId);
        film.getLikes().remove(user.getId());

        return filmStorage.getFilmById(id);
    }

    public List<Film> getPopularFilms(int count) {
        if (count <= 0) throw new ValidateException("Введите количество фильмов больше 0");

        List<Film> films = filmStorage.getPopularFilms(count);
        List<Film> result = new ArrayList<>();

        for (Film film : films) {
            result.add(filmStorage.getFilmById(film.getId()));
        }

        return result;
    }


    public Film createFilm(Film film) {
        filmValidate(film);
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        filmValidate(film);
        if (film.getId() <= 0) {
            throw new IllegalArgumentException("Film id must be > 0");
        }
       return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(long id) {
        return filmStorage.getFilmById(id);
    }

    public void deleteFilmById(long id) {
         filmStorage.deleteFilmById(id);
    }

    public void deleteAllFilms() {
        filmStorage.deleteAllFilms();
    }

    public List<Genre> getAllGenre() {
        return filmStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        if (id < 1 || id > 6) {
            throw new GenreNotFoundException("id жанра должен быть от 1 до 6");
        }

        return filmStorage.getGenreById(id);
    }

    public List<Mpa> getAllMpa() {
        return filmStorage.getAllMpa();
    }

    public Mpa getMpaById(int id) {
        if (id < 1 || id > 5) {
            throw new MpaNotFoundException("id mpa должен быть от 1 до 5");
        }

        return filmStorage.getMpaById(id);
    }

    private final void filmValidate(Film film) throws ValidateException {
        if (film == null) {
            throw new FilmNotFoundException("Фильм не может быть null");
        }

        if (film.getName().isBlank()) {
            throw new ValidateException("название фильма не может быть пустым или состоять из пробелов");
        }

        if (film.getDescription().length() > 200) {
            throw new ValidateException("максимальная длинна фильма не должна превышать 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 28))) {
            throw new ValidateException("дата релиза должна быть не раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidateException("продолжительность фильма должна быть положительной");
        }

        if (film.getMpa() == null || film.getMpa().getId() < 1 || film.getMpa().getId() > 5) {
            throw new CommonIdException("рейтинг id должен быть от 1 до 5");
        }


        Set<Long> allowedGenreIds = Set.of(1L, 2L, 3L, 4L, 5L, 6L);
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!allowedGenreIds.contains(genre.getId())) {
                    throw new CommonIdException("Жанр с id " + genre.getId() + " не существует");
                }
            }
        }

    }
}
