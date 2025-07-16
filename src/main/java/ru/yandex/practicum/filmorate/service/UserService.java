package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FriendAlreadyExist;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
            throw new IllegalArgumentException("нельзя добавлять самого себя в друзья");
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
