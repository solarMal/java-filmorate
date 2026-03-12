package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.CommonIdException;
import ru.yandex.practicum.filmorate.exception.FriendAlreadyExist;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private int dynamicId = 1;
    Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User user) {
        userValidate(user);
        user.setId(dynamicId++);
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно создан", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        userValidate(user);
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new UserNotFoundException("Пользователь с id " + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} обновлён", user.getId());
        return user;
    }

    @Override
    public User getUserById(long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new UserNotFoundException("пользователь не найден"));
    }


    @Override
    public List<User> getUsers() {
        if (users.isEmpty()) {
            log.debug("нет активных пользователей");
        }

        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteUserById(long id) {
        users.remove(id);
    }

    @Override
    public void deleteAllUsers() {
        users.clear();
    }

    @Override
    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new CommonIdException("нельзя добавлять самого себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user.getFriendsId().contains(friendId)) {
            throw new FriendAlreadyExist("данный пользователь уже ваш друг");
        }

        user.getFriendsId().add(friendId);
        friend.getFriendsId().add(userId);

        log.info("вы и пользователь с id {} теперь друзья", friendId);
    }

    @Override
    public void addFriendRequest(long userId, long requesterId) {

    }

    @Override
    public void deleteFriendRequest(long userId, long requesterId) {

    }

    @Override
    public boolean isFriendRequestExists(long userId, long requesterId) {
        return false;
    }

    @Override
    public void deleteFriend(long userId, long friendId) {

    }

    @Override
    public Set<Long> getFriendsId(long userId) {
        User user = users.get(userId);

        if (user == null) {
            throw new UserNotFoundException("пользователь не найден");
        }
        return new HashSet<>(user.getFriendsId());
    }


    private void userValidate(User user) throws ValidateException {
        if (user == null) {
            throw new NullPointerException("Пользователь не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidateException("Email не может быть пустым и должен содержать символ '@'");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidateException("Login не может быть пустым и не должен содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidateException("Дата рождения не может быть в будущем");
        }
    }
}
