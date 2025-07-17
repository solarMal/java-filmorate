package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.CommonIdException;
import ru.yandex.practicum.filmorate.exception.FriendAlreadyExist;
import ru.yandex.practicum.filmorate.exception.FriendNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(long id, long friendId) {
        if (id == friendId) {
            throw new CommonIdException("нельзя добавлять самого себя в друзья");
        }

        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        if (user.getFriendsId().contains(friendId)) {
            throw new FriendAlreadyExist("данный пользователь уже ваш друг");
        }

        user.getFriendsId().add(friendId);
        friend.getFriendsId().add(id);

        log.info("вы и пользователь с id {} теперь друзья", friendId);

        return user;
    }

    public User deleteFriend(long id, long friendId) {
        if (id == friendId) {
            throw new CommonIdException("вы не можете удалить себя из друзей");
        }

        User user = userStorage.getUserById(id);
        User friend = userStorage.getUserById(friendId);

        if (!user.getFriendsId().contains(friendId)) {
            throw new FriendNotFoundException("пользователь с id " + friendId + " не является вашим другом ");
        }

        user.getFriendsId().remove(friendId);
        friend.getFriendsId().remove(id);

        log.info("вы и пользователь с id {} теперь не друзья", friendId);

        return user;
    }

    public Set<User> getAllFriends(long id) {
        User user = userStorage.getUserById(id);

        if (user.getFriendsId().isEmpty()) {
            return Collections.emptySet();
        }

        return user.getFriendsId().stream()
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
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }
}
