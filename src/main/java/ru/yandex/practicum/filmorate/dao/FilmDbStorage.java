package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {

        String sql = "INSERT INTO film (name, description, release_date, duration, rate) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRate());
            return ps;
        }, keyHolder);

        long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            Set<Long> uniqueGenres = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            for (Long genreId : uniqueGenres) {
                jdbcTemplate.update(sqlGenre, filmId, genreId);
            }
        }

        if (film.getMpa() != null) {
            String sqlMpa = "INSERT INTO film_mpa (film_id, mpa_id) VALUES (?, ?)";
            jdbcTemplate.update(sqlMpa, filmId, film.getMpa().getId());
        }

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rate = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getRate(),
                film.getId()
        );
        if (rows == 0) throw new FilmNotFoundException("Фильм с id " + film.getId() + " не найден");

        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            String sqlGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Long genreId : uniqueGenreIds) {
                jdbcTemplate.update(sqlGenre, film.getId(), genreId);
            }
        }

        jdbcTemplate.update("DELETE FROM film_mpa WHERE film_id = ?", film.getId());
        if (film.getMpa() != null) {
            jdbcTemplate.update("INSERT INTO film_mpa (film_id, mpa_id) VALUES (?, ?)", film.getId(), film.getMpa().getId());
        }

        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM film";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            long filmId = film.getId();

            Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(
                    "SELECT user_id FROM film_likes WHERE film_id = ?", Long.class, filmId));
            film.setLikes(likes);

            List<Genre> genres = jdbcTemplate.query(
                    "SELECT g.id, g.name " +
                            "FROM genre g " +
                            "JOIN film_genre fg ON g.id = fg.genre_id " +
                            "WHERE fg.film_id = ? " +
                            "ORDER BY g.id",
                    genreRowMapper, filmId
            );
            film.setGenres(new LinkedHashSet<>(genres));

            String sqlMpa = "SELECT m.id, m.mpa_name " +
                    "FROM mpa m " +
                    "JOIN film_mpa fm ON m.id = fm.mpa_id " +
                    "WHERE fm.film_id = ?";
            Mpa mpa = jdbcTemplate.query(sqlMpa, mpaRowMapper, filmId)
                    .stream().findFirst().orElse(null);
            film.setMpa(mpa);
        }

        return films;
    }

    @Override
    public Film getFilmById(long id) {
        String sql = "SELECT * FROM film WHERE id = ?";

        Film film = jdbcTemplate.query(sql, filmRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new FilmNotFoundException("Фильм с id " + id + " не найден"));

        Set<Long> likes = new HashSet<>(jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ?", Long.class, id));
        film.setLikes(likes);

        List<Genre> genres = (jdbcTemplate.query(
                "SELECT * FROM genre AS g " +
                        "JOIN film_genre AS fg ON g.id=fg.genre_id " +
                        "JOIN film AS f ON fg.film_id=f.id " +
                        "WHERE f.id = ? " +
                        "ORDER BY g.id",
                genreRowMapper, id
        ));
        film.setGenres(new LinkedHashSet<>(genres));

        String sqlMpa = "SELECT * from mpa AS a" +
                " JOIN film_mpa AS fm ON a.id=fm.mpa_id" +
                " JOIN film AS f ON fm.film_id=f.id" +
                " WHERE f.id = ?";

        Mpa mpa = jdbcTemplate.query(sqlMpa, mpaRowMapper, id).stream().findFirst().orElse(null);
        film.setMpa(mpa);

        return film;
    }

    @Override
    public void deleteFilmById(long id) {
        String sql = "DELETE FROM film WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public void deleteAllFilms() {
        String sql = "DELETE FROM film";

        jdbcTemplate.update(sql);
    }

    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ? ";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        if (count <= 0) throw new ValidateException("Введите количество фильмов больше 0");

        String sql = """
        SELECT f.id, COUNT(fl.user_id) AS likes_count
        FROM film f
        LEFT JOIN film_likes fl ON f.id = fl.film_id
        GROUP BY f.id
        ORDER BY likes_count DESC, f.id DESC
        LIMIT ?
        """;

        List<Long> filmIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"), count);
        List<Film> result = new ArrayList<>();

        for (Long id : filmIds) {
            result.add(getFilmById(id));
        }

        return result;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genre ORDER BY genre.id";

        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Genre getGenreById(int id) {
        String sql = "SELECT * FROM genre WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Жанр с id " + id + " не найден");
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa ORDER BY mpa.id";

        return jdbcTemplate.query(sql, mpaRowMapper);
    }

    @Override
    public Mpa getMpaById(int id) {
        String sql = "SELECT * FROM mpa WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new MpaNotFoundException("Mpa c id " + id + " не найден");
        }
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) ->
            new Film(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getObject("release_date", LocalDate.class),
                    rs.getInt("duration"),
                    rs.getInt("rate")
            );

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> new Genre(
            rs.getLong("id"),
            rs.getString("name")
    );

    private final RowMapper<Mpa> mpaRowMapper = (rs, rowNum) -> new Mpa(
            rs.getLong("id"),
            rs.getString("mpa_name")
    );


}
