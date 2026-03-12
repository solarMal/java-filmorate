package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());

            return ps;
        }, keyHolder);

        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();

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
            throw new UserNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        return user;
    }


    @Override
    public User getUserById(long id) {
        String sqlUser = "SELECT * FROM app_user WHERE id = ?";
        //при использовании jdbcTemplate.query бросается UserNotFoundException, а если queryForObject бросается
        //EmptyResultDataAccessException
        User user = jdbcTemplate.query(sqlUser, userRowMapper, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id " + id + " не найден"));

        Set<Long> friends = new HashSet<>(jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id = ?", Long.class, id));
        user.setFriendsId(friends);

        Set<Long> requests = new HashSet<>(jdbcTemplate.queryForList(
                "SELECT friend_id_request FROM friend_request WHERE user_id = ?", Long.class, id));
        user.setFriendRequest(requests);

        return user;
    }

    @Override
    public List<User> getUsers() {
        String sqlUser = "SELECT * FROM app_user";
        List<User> users = jdbcTemplate.query(sqlUser, userRowMapper);

        for (User user : users) {
            long id = user.getId();

            String sqlFriends = "SELECT friend_id FROM friends WHERE user_id = ?";
            user.setFriendsId(new HashSet<>(jdbcTemplate.queryForList(sqlFriends, Long.class, id)));

            String sqlRequests = "SELECT friend_id_request FROM friend_request WHERE user_id = ?";
            user.setFriendRequest(new HashSet<>(jdbcTemplate.queryForList(sqlRequests, Long.class, id)));
        }

        return users;
    }

    public void deleteUserById(long id) {
        String sql = "DELETE FROM app_user WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    public void deleteAllUsers() {
        String sql = "DELETE from app_user";

        jdbcTemplate.update(sql);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public void addFriendRequest(long userId, long requesterId) {
        String sql = "INSERT INTO friend_request (user_id, friend_id_request) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, requesterId);
    }

    public void deleteFriendRequest(long userId, long requesterId) {
        String sql = "DELETE FROM friend_request WHERE user_id = ? AND friend_id_request = ?";
        jdbcTemplate.update(sql, userId, requesterId);
    }

    public boolean isFriendRequestExists(long userId, long requesterId) {
        String sql = "SELECT COUNT(*) FROM friend_request WHERE user_id = ? AND friend_id_request = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, requesterId);
        return count > 0;
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Set<Long> getFriendsId(long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";

        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
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
