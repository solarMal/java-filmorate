package ru.yandex.practicum.filmorate.daoimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.UserExistsException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.validated.UserValidated;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private int nextUserId = 1;

    private final JdbcTemplate jdbcTemplate;
    UserValidated userValidated = new UserValidated();

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseEntity<User> createUser(User user) {
        userValidated.allUserValidate(user);

        if (userExists(user.getLogin())) {
            throw new UserExistsException("Пользователь с таким логином уже существует");
        }

        user.setId(nextUserId++);

        String sql = "INSERT INTO users (id, login, name, email, birthday) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getId(), user.getLogin(), user.getName(), user.getEmail(), user.getBirthday());

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Override
    public ResponseEntity<?> updateUser(User user) {
        userValidated.allUserValidate(user);
        int userId = user.getId();

        User existingUser = getUserById((long)userId);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String sql = "UPDATE users SET login = ?,name = ? ,email = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getLogin(), user.getName(), user.getEmail(), user.getBirthday(), userId);
        return ResponseEntity.ok().body(user);
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        String userSql = "SELECT * FROM Users";
        List<User> users = jdbcTemplate.query(userSql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            // Получение списка запросов в друзья
            String requestSql = "SELECT user_id FROM Friendship_request WHERE friend_id = ?";
            List<Long> requestIds = jdbcTemplate.queryForList(requestSql, Long.class, user.getId());
            Set<Long> requestsSet = new HashSet<>(requestIds);
            user.setFriendshipRequests(requestsSet);

            // Получение списка друзей
            String friendSql = "SELECT friend_id FROM Friend WHERE user_id = ?";
            List<Long> friendIds = jdbcTemplate.queryForList(friendSql, Long.class, user.getId());
            Set<Long> friendsSet = new HashSet<>(friendIds);
            user.setFriendsId(friendsSet);

            return user;
        });

        return ResponseEntity.ok(users);
    }

    @Override
    public Set<User> getUsers() {
        return null;
    }

    public void createFriendshipRequest(User user, User friend) throws Exception {
        if (user.getId().equals(friend.getId())){
            throw new Exception("нельзя добавить в друзья себя");
        }

        if (user.getFriendsId().contains((long) friend.getId())) {
            return;
        }

        if (user.getFriendshipRequests().contains((long) friend.getId())) {
            return;
        }

        String insertSql = "INSERT INTO Friendship_request (user_id, friend_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(insertSql, user.getId(), friend.getId());

            friend.getFriendshipRequests().add((long) user.getId());
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void acceptFriendshipRequest(User user, User friend) {
        if (user.getFriendsId().contains((long) friend.getId())) {
            log.info("Друг {} уже присутствует в списке друзей пользователя {}. Отмена операции.",
                    friend.getId(), user.getId());
            return;
        }

        user.getFriendsId().add((long) friend.getId());

        String deleteSql = "DELETE FROM Friendship_request WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(deleteSql, friend.getId(), user.getId());

        String insertSql = "INSERT INTO Friend (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, user.getId(), friend.getId());

        addFriend(user, friend);
    }

    private void addFriend(User user, User friend) {
        if (friend.getFriendsId().contains((long) user.getId())) {
            throw new IllegalArgumentException("User is already a friend.");
        }

        friend.getFriendsId().add((long) user.getId());
    }

    public User getUserById(Long userId) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        Object[] params = { userId };
        List<User> users = jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());

            // Retrieve friendsId from the Friend table
            String friendsSql = "SELECT friend_id FROM Friend WHERE user_id = ?";
            List<Long> friendsIdList = jdbcTemplate.queryForList(friendsSql, new Object[]{userId}, Long.class);
            Set<Long> friendsIdSet = new HashSet<>(friendsIdList);
            user.setFriendsId(friendsIdSet);

            // Retrieve friendshipRequests from the Friendship_request table
            String requestsSql = "SELECT user_id FROM Friendship_request WHERE friend_id = ?";
            List<Long> requestsIdList = jdbcTemplate.queryForList(requestsSql, new Object[]{userId}, Long.class);
            Set<Long> requestsIdSet = new HashSet<>(requestsIdList);
            user.setFriendshipRequests(requestsIdSet);

            return user;
        });

        if (!users.isEmpty()) {
            return users.get(0);
        } else {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    public void removeFriend(User user, User friend) {
        long friendId = friend.getId();
        long userId = user.getId();

        String deleteSql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, userId, friendId);

        if (rowsAffected > 0) {
            user.getFriendsId().remove(friendId);
            friend.getFriendsId().remove(userId);
            log.info("Friend removed: User={}, Friend={}", user, friend);
        }
    }

    public Set<User> getFriendsList(User user) {
        Set<User> friendsList = new LinkedHashSet<>();

        Set<Long> friendsId = user.getFriendsId();

        for (Long friendId : friendsId) {
            String selectSql = "SELECT * FROM users WHERE id = ?";
            User friend = jdbcTemplate.queryForObject(selectSql, new Object[]{friendId}, (rs, rowNum) -> {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setLogin(rs.getString("login"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                u.setBirthday(rs.getDate("birthday").toLocalDate());
                return u;
            });

            if (friend != null) {
                friendsList.add(friend);
                log.info("FriendList: Friend={}", friend);
            }
        }

        return friendsList;
    }

    public Set<User> getListMutualFriends(User user, User otherUser) {
        Set<User> mutualFriends = new HashSet<>();

        Set<Long> userFriendsId = user.getFriendsId();
        Set<Long> otherUserFriendsId = otherUser.getFriendsId();

        Set<Long> mutualFriendIds = new HashSet<>(userFriendsId);
        mutualFriendIds.retainAll(otherUserFriendsId);

        for (Long friendId : mutualFriendIds) {
            String selectSql = "SELECT * FROM users WHERE id = ?";
            User friend = null;
            try {
                friend = jdbcTemplate.queryForObject(selectSql, new Object[]{friendId}, (rs, rowNum) -> {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setLogin(rs.getString("login"));
                    u.setName(rs.getString("name"));
                    u.setEmail(rs.getString("email"));
                    u.setBirthday(rs.getDate("birthday").toLocalDate());
                    return u;
                });
            } catch (Exception e) {
                log.error("Ошибка при получении данных о друге с ID {}: {}", friendId, e.getMessage());
            }

            if (friend != null) {
                mutualFriends.add(friend);
            }
        }

        log.info("Количество общих друзей: {}", mutualFriends.size());

        return mutualFriends;
    }

    private boolean userExists(String login) {
        String selectUserSql = "SELECT COUNT(*) FROM users WHERE login = ?";
        int count = jdbcTemplate.queryForObject(selectUserSql, Integer.class, login);
        return count > 0;
    }
}
