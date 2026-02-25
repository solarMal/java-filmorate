package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        String sql = "INSERT INTO app_user (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());

            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();

        return new User(
                generatedId,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE app_user SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";

        int rowAffected = jdbcTemplate.update(
          sql,
          user.getEmail(),
          user.getLogin(),
          user.getEmail(),
          user.getBirthday(), user.getId()
        );

        if (rowAffected == 0) {
            throw new RuntimeException("Пользователь с id " + user.getId() + " не найден");
        }

        return user;
    }

    @Override
    public User getUserById(long id) {
        String sql = "SELECT * FROM app_user WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public List<User> getUsers() {
        return List.of();
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) ->
            new User(
                    rs.getLong("id"),
                    rs.getString("email"),
                    rs.getString("login"),
                    rs.getString("name"),
                    rs.getObject("birthday", LocalDate.class)
            );
}
