package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.controller.ErrorHandler;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.InvalidLoginException;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    public UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }


    public User getUserById(Long userId) {
        for (User user : userStorage.getUsers()) {
            if (user.getId() == userId) {
                return user;
            }
        }
        throw new UserNotFoundException("User not found with ID: " + userId);
    }

    public void addUser(User user) {
        try {
            userStorage.createUser(user);
        } catch (InvalidLoginException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user");
        }
    }

    public ResponseEntity<?> updateUser(User user){
        userStorage.updateUser(user);
        log.info("User update: {}", user);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    public ResponseEntity<Set<User>> getAllUsers() {
        log.info("количество пользователей в текущий момент: {}", userStorage.getUsers());
        return new ResponseEntity<>(userStorage.getUsers(), HttpStatus.OK);
    }


    public void addFriend(User user, User friend) {
        if (user.getFriendsId().contains((long) friend.getId())) {
            return;
        }

        if (user.getFriendsId().isEmpty()) {
            user.setFriendsId(new HashSet<>());
        }
        if (friend.getFriendsId().isEmpty()) {
            friend.setFriendsId(new HashSet<>());
        }

        user.getFriendsId().add((long) friend.getId());
        friend.getFriendsId().add((long) user.getId());
    }

    public void removeFriend(User user, User friend) {
        long friendId = friend.getId();
        long userId = user.getId();

        if (user.getFriendsId().contains(friendId)){
            if (user.getFriendsId().remove(friendId)) {
                friend.getFriendsId().remove(userId);
                log.info("Friend removed: User={}, Friend={}", user, friend);
            }
        }
    }

    public Set<User> getFriendsList(User user) {
        Set<User> friendsList = new HashSet<>();

        Set<Long> friendsId = user.getFriendsId();
        for (Long friendId : friendsId) {
            User friend = getUserById(friendId);
            if (friend != null) {
                friendsList.add(friend);
                log.info("FriendList: Friend={}", friend);
            }
        }

        return friendsList;
    }

    public Set<User> getListMutualFriends(User user, User otherUser){
        Set<User> mutualFriends = new HashSet<>();
        for (Long userId : user.getFriendsId()){
            for (Long otherUserId : otherUser.getFriendsId()){
                if (userId.equals(otherUserId)){
                    User friend = getUserById(userId);
                    mutualFriends.add(friend);
                }
            }
        }
        return mutualFriends;
    }
}
