package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CommonIdException;
import ru.yandex.practicum.filmorate.exception.FriendAlreadyExist;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidateException("Нельзя добавить себя в друзья");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        Set<Long> friendsInDb = userStorage.getFriendsId(userId);
        if (friendsInDb.contains(friendId)) {
            throw new FriendAlreadyExist("Друг уже существует");
        }

        if (userStorage.isFriendRequestExists(userId, friendId)) {
            userStorage.addFriend(userId, friendId);
            userStorage.deleteFriendRequest(userId, friendId);
            return List.of(user, friend);
        }

        userStorage.addFriend(userId, friendId);
        userStorage.addFriendRequest(friendId, userId);

        return List.of(getUserById(user.getId()),getUserById(friend.getId()));
    }

    public User deleteFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new CommonIdException("вы не можете удалить себя из друзей");
        }

        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        userStorage.deleteFriend(user.getId(), friend.getId());
        user.getFriendsId().remove(friend.getId());
        userStorage.deleteFriendRequest(user.getId(), friend.getId());
        user.getFriendRequest().remove(friend.getId());

        log.info("вы и пользователь с id {} теперь не друзья", friendId);

        return getUserById(user.getId());
    }

    public Set<User> getAllFriends(long id) {

        User user = userStorage.getUserById(id);

        Set<Long> friendIds = userStorage.getFriendsId(id);

        return friendIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(long id, long otherId) {
        if (id == otherId) {
            throw new CommonIdException("вы ввели свой Id");
        }

        User user = userStorage.getUserById(id);
        User other = userStorage.getUserById(otherId);

        Set<Long> commonIds = new HashSet<>(user.getFriendsId());
        commonIds.retainAll(other.getFriendsId());

        return commonIds.stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toSet());
    }

    public User createUser(User user) {
        userValidate(user);
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        userValidate(user);

        return userStorage.updateUser(user);
    }

    public User getUserById(long id) {
        if (id <= 0) {
            throw new CommonIdException("id должен быть положительным");
        }
        return userStorage.getUserById(id);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public void deleteUserById(long id) {
        if (id <= 0) {
            throw new CommonIdException("введите id больше 0");
        }
        userStorage.deleteUserById(id);
    }

    public void deleteAllUsers() {
        userStorage.deleteAllUsers();
    }

    private final void userValidate(User user) throws ValidateException {
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
