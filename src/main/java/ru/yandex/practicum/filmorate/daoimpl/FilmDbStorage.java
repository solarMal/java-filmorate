package ru.yandex.practicum.filmorate.daoimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.validated.FilmValidated;

import java.util.*;

@Validated
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    FilmValidated filmValidated = new FilmValidated();
    private long nextFilmId = 1;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final JdbcTemplate jdbcTemplate;
    UserDbStorage userDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, UserDbStorage userDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDbStorage = userDbStorage;
    }

    @Override
    public ResponseEntity<Film> createFilm(Film film) {
        try {
            filmValidated.validateAll(film);

            List<FilmGenre> genres = film.getGenres();
            if (genres != null) {
                for (FilmGenre genre : genres) {
                    Integer genreId = genre.getId();
                    if (genreId.equals(0)) {
                        String errorMessage = "Неверный ID жанра: " + genreId;
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(film);
                    }
                }
            }

            film.setId(nextFilmId++);

            String insertFilmSql = "INSERT INTO Film (name, releaseDate, description, duration, film_mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

            String insertFilmGenreSql = "INSERT INTO FilmGenre (film_id, genre_id) VALUES (?, ?)";

            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("Film").usingGeneratedKeyColumns("id");

            Map<String, Object> filmParams = new HashMap<>();
            filmParams.put("name", film.getName());
            filmParams.put("releaseDate", film.getReleaseDate());
            filmParams.put("description", film.getDescription());
            filmParams.put("duration", film.getDuration());
            filmParams.put("film_mpa_id", film.getMpa().getId());

            int filmId = jdbcInsert.executeAndReturnKey(filmParams).intValue();

            if (genres != null) {
                for (FilmGenre genre : genres) {
                    Integer genreId = genre.getId();
                    if (genreId.equals(0)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(film);
                    }
                    jdbcTemplate.update(insertFilmGenreSql, filmId, genreId);

                    String selectGenreNameSql = "SELECT name FROM Genre WHERE id = ?";
                    String genreName = jdbcTemplate.queryForObject(selectGenreNameSql, String.class, genreId);
                    genre.setName(genreName);
                }
            }

            String selectRatingSql = "SELECT name FROM FilmMPA WHERE id = ?";
            String rating = jdbcTemplate.queryForObject(selectRatingSql, String.class, film.getMpa().getId());
            film.getMpa().setName(rating);

            String selectLikeCountSql = "SELECT COUNT(*) FROM filmLike WHERE filmId = ?";
            Long likeCount = jdbcTemplate.queryForObject(selectLikeCountSql, Long.class, filmId);
            film.setFilmLikeByUserId(Collections.singleton(likeCount));

            log.info("Фильм успешно вставлен в базу данных: {}", film);

        } catch (ValidationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(film);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @Override
    public ResponseEntity<Film> updateFilm(Film updatedFilm) {
        try {
            filmValidated.validateAll(updatedFilm);

            List<FilmGenre> genres = updatedFilm.getGenres();
            if (genres != null) {
                for (FilmGenre genre : genres) {
                    Integer genreId = genre.getId();
                    if (genreId.equals(0)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(updatedFilm);
                    }
                }
            }

            Long id = updatedFilm.getId();

            String updateSql = "UPDATE Film SET name = ?, description = ?, releaseDate = ?, duration = ?, film_mpa_id = ? WHERE id = ?";
            int rowsUpdated = jdbcTemplate.update(updateSql, updatedFilm.getName(), updatedFilm.getDescription(),
                    updatedFilm.getReleaseDate(), updatedFilm.getDuration(), updatedFilm.getMpa().getId(), id);

            if (rowsUpdated == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(updatedFilm);
            }

            String deleteFilmGenreSql = "DELETE FROM FilmGenre WHERE film_id = ?";
            jdbcTemplate.update(deleteFilmGenreSql, id);

            String insertFilmGenreSql = "INSERT INTO FilmGenre (film_id, genre_id) VALUES (?, ?)";
            if (updatedFilm.getGenres() != null) {
                Set<Integer> uniqueGenreIds = new HashSet<>();
                List<FilmGenre> distinctGenres = new ArrayList<>();

                for (FilmGenre genre : updatedFilm.getGenres()) {
                    if (uniqueGenreIds.add(genre.getId())) {
                        distinctGenres.add(genre);
                    }
                }

                updatedFilm.setGenres(distinctGenres);

                for (FilmGenre genre : distinctGenres) {
                    jdbcTemplate.update(insertFilmGenreSql, id, genre.getId());
                }
            }

            String selectRatingSql = "SELECT name FROM FilmMPA WHERE id = ?";
            String rating = jdbcTemplate.queryForObject(selectRatingSql, String.class, updatedFilm.getMpa().getId());
            updatedFilm.getMpa().setName(rating);

            log.info("Информация о обновленных фильмах: {}", updatedFilm);

            return ResponseEntity.ok(updatedFilm);
        } catch (ValidationException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(updatedFilm);
        }
    }


    public ResponseEntity<List<Film>> getAllFilms() {
        try {
            String selectFilmsSql = "SELECT * FROM Film";

            List<Film> films = jdbcTemplate.query(selectFilmsSql, (rs, rowNum) -> {
                Film film = new Film();
                film.setId(rs.getLong("id"));
                film.setName(rs.getString("name"));
                film.setDescription(rs.getString("description"));
                film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                film.setDuration(rs.getInt("duration"));
                int mpaId = rs.getInt("film_mpa_id");
                film.setMpa(new FilmMPA(mpaId));
                film.setGenres(new ArrayList<>());
                return film;
            });

            for (Film film : films) {
                String selectGenresSql = "SELECT g.id, g.name FROM Genre g JOIN FilmGenre fg ON g.id = fg.genre_id" +
                        " WHERE fg.film_id = ?";
                List<FilmGenre> genres = jdbcTemplate.query(selectGenresSql, (rs, rowNum) -> {
                    FilmGenre genre = new FilmGenre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }, film.getId());
                film.setGenres(genres);

                String selectMpaRatingSql = "SELECT name FROM FilmMPA WHERE id = ?";
                String rating = jdbcTemplate.queryForObject(selectMpaRatingSql, String.class, film.getMpa().getId());
                film.getMpa().setName(rating);

                String selectLikeSql = "SELECT userId FROM filmLike WHERE filmId = ?";
                List<Long> likedUserIds = jdbcTemplate.queryForList(selectLikeSql, Long.class, film.getId());
                Set<Long> filmLikeByUserId = new HashSet<>(likedUserIds);
                film.setFilmLikeByUserId(filmLikeByUserId);
            }

            log.info("Получен список всех фильмов");

            return ResponseEntity.ok(films);
        } catch (Exception exception) {
            log.error("Ошибка при получении списка фильмов: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Film getFilmById(Long filmId) {
        try {
            String selectFilmSql = "SELECT f.*, m.name AS mpaName " +
                    "FROM Film f " +
                    "LEFT JOIN FilmMPA m ON f.film_mpa_id = m.id " +
                    "WHERE f.id = ?";

            Film film = jdbcTemplate.queryForObject(selectFilmSql, (rs, rowNum) -> {
                Film filmResult = new Film();
                filmResult.setId(filmId);
                filmResult.setName(rs.getString("name"));
                filmResult.setDescription(rs.getString("description"));
                filmResult.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                filmResult.setDuration(rs.getInt("duration"));

                FilmMPA mpa = new FilmMPA();
                mpa.setId(rs.getInt("film_mpa_id"));
                mpa.setName(rs.getString("mpaName"));
                filmResult.setMpa(mpa);

                return filmResult;
            }, filmId);

            if (film != null) {
                String selectGenresSql = "SELECT g.id, g.name " +
                        "FROM Genre g " +
                        "JOIN FilmGenre fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ?";

                List<FilmGenre> genres = jdbcTemplate.query(selectGenresSql, (rs, rowNum) -> {
                    FilmGenre genre = new FilmGenre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }, filmId);

                film.setGenres(genres);

                String selectLikesSql = "SELECT userId FROM filmLike WHERE filmId = ?";
                List<Long> likeIds = jdbcTemplate.queryForList(selectLikesSql, Long.class, filmId);
                Set<Long> filmLikeByUserId = new HashSet<>(likeIds);
                film.setFilmLikeByUserId(filmLikeByUserId);
            }

            return film;

        } catch (Exception exception) {
            log.error("Ошибка при получении фильма с ID {}: {}", filmId, exception.getMessage());
        }

        return null;
    }

    @Override
    public List<Film> getFilms() {
        return null;
    }

    public ResponseEntity<?> addFilmLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);

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

        try {
            String insertSql = "INSERT INTO filmLike (filmId, userId) VALUES (?, ?)";
            jdbcTemplate.update(insertSql, filmId, userId);

            // Retrieve updated film with likes
            Film updatedFilm = getFilmById(filmId);

            log.info("Film like added. Film ID: {}, User ID: {}", filmId, userId);
            return ResponseEntity.ok(updatedFilm);
        } catch (Exception e) {
            log.error("Failed to add film like. Film ID: {}, User ID: {}", filmId, userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<?> removeFilmLike(long filmId, long userId) {
        Film film = getFilmById(filmId);
        User user = userDbStorage.getUserById(userId);

        if (film == null) {
            return ResponseEntity.notFound().build();
        }

        if (user == null) {
            return ResponseEntity.badRequest().body("Invalid user ID.");
        }

        if (!film.getFilmLikeByUserId().contains(userId)) {
            return ResponseEntity.badRequest().body("User has not liked this film.");
        }

        film.getFilmLikeByUserId().remove(userId);
        updateFilm(film);

        try {
            String deleteSql = "DELETE FROM filmLike WHERE filmId = ? AND userId = ?";
            int deletedRows = jdbcTemplate.update(deleteSql, filmId, userId);

            if (deletedRows > 0) {
                updateFilm(film);

                log.info("Film like removed. Film ID: {}, User ID: {}", filmId, userId);
                return ResponseEntity.ok().build();
            } else {
                log.error("Failed to remove film like. Film ID: {}, User ID: {}", filmId, userId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            log.error("Failed to remove film like. Film ID: {}, User ID: {}", filmId, userId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public List<Film> getPopularFilms(int count) {
        String selectFilmsSql = "SELECT f.*, COUNT(fl.userId) AS likeCount " +
                "FROM Film f LEFT JOIN filmLike fl ON f.id = fl.filmId " +
                "GROUP BY f.id " +
                "ORDER BY likeCount DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(selectFilmsSql, (rs, rowNum) -> {
            Film filmResult = new Film();
            filmResult.setId(rs.getLong("id"));
            filmResult.setName(rs.getString("name"));
            filmResult.setDescription(rs.getString("description"));
            filmResult.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
            filmResult.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("film_mpa_id");
            FilmMPA mpa = getFilmMPAById(mpaId);
            filmResult.setMpa(mpa);

            List<FilmGenre> genres = getFilmGenresByFilmId(filmResult.getId());
            filmResult.setGenres(genres);

            Set<Long> filmLikeByUserId = new HashSet<>();
            filmLikeByUserId.add(rs.getLong("likeCount"));
            filmResult.setFilmLikeByUserId(filmLikeByUserId);

            return filmResult;
        }, count);

        return films;
    }

    public List<FilmMPA> getAllFilmMPAsFromDatabase() {
        String selectFilmMPAsSql = "SELECT * FROM FilmMPA";
        return jdbcTemplate.query(selectFilmMPAsSql, (rs, rowNum) -> {
            FilmMPA mpa = new FilmMPA();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    public FilmMPA getFilmMPAFromDatabaseById(int id) {
        String selectFilmMPASql = "SELECT * FROM FilmMPA WHERE id = ?";
        List<FilmMPA> filmMPAs = jdbcTemplate.query(selectFilmMPASql, (rs, rowNum) -> {
            FilmMPA mpa = new FilmMPA();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }, id);

        if (!filmMPAs.isEmpty()) {
            return filmMPAs.get(0);
        } else {
            return null;
        }
    }

    public List<FilmGenre> getAllGenresFromDatabase() {
        String selectGenresSql = "SELECT * FROM Genre";
        return jdbcTemplate.query(selectGenresSql, (rs, rowNum) -> {
            FilmGenre genre = new FilmGenre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        });
    }

    public FilmGenre getGenreFromDatabaseById(int id) {
        String selectGenreSql = "SELECT * FROM Genre WHERE id = ?";
        List<FilmGenre> genres = jdbcTemplate.query(selectGenreSql, (rs, rowNum) -> {
            FilmGenre genre = new FilmGenre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, id);

        if (!genres.isEmpty()) {
            return genres.get(0);
        } else {
            return null;
        }
    }

    private FilmMPA getFilmMPAById(int mpaId) {
        String selectMpaSql = "SELECT * FROM FilmMPA WHERE id = ?";
        return jdbcTemplate.queryForObject(selectMpaSql, (rs, rowNum) -> {
            FilmMPA mpa = new FilmMPA();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        }, mpaId);
    }

    private List<FilmGenre> getFilmGenresByFilmId(Long filmId) {
        String selectGenresSql = "SELECT g.id, g.name FROM Genre g " +
                "JOIN FilmGenre fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbcTemplate.query(selectGenresSql, (rs, rowNum) -> {
            FilmGenre genre = new FilmGenre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);
    }

    private boolean filmExists(Film film) {
        String selectFilmSql = "SELECT COUNT(*) FROM Film WHERE name = ? AND releaseDate = ?";
        int count = jdbcTemplate.queryForObject(selectFilmSql, Integer.class, film.getName(), film.getReleaseDate());
        return count > 0;
    }

}
