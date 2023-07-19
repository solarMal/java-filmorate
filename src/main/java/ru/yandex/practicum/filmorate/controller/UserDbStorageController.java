package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.daoimpl.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

@RestController
@RequestMapping("/users")
@Validated
public class UserDbStorageController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    UserDbStorage userDbStorage;

    public UserDbStorageController(@Qualifier("UserDbStorage") UserDbStorage userDbStorage) {
        this.userDbStorage = userDbStorage;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        log.info("юзер добавлен" + user);
        return userDbStorage.createUser(user);
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        log.info("юзер обновлён" + user);
        return userDbStorage.updateUser(user);
    }

    @PutMapping("/{id}/createFriendshipRequest/{friendId}")
    public ResponseEntity<Void> createFriendshipRequest(@PathVariable("id") Long userId,
                                                        @PathVariable("friendId") Long friendId) throws Exception {
        User user = userDbStorage.getUserById(userId);
        User friend = userDbStorage.getUserById(friendId);

        userDbStorage.createFriendshipRequest(user, friend);

        log.info("Friendship request created: User={}, Friend={}", user.getId(), friend.getId());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> acceptFriendshipRequest(@PathVariable("id") Long userId,
                                                        @PathVariable("friendId") Long friendId) {
        User user = userDbStorage.getUserById(userId);
        User friend = userDbStorage.getUserById(friendId);

        userDbStorage.acceptFriendshipRequest(user, friend);

        log.info("Friendship request accepted: User={}, Friend={}", user.getId(), friend.getId());

        return ResponseEntity.ok().build();
    }


    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userDbStorage.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return userDbStorage.getAllUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable int userId) {
        try {
            userDbStorage.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable("userId") Long userId, @PathVariable("friendId") Long friendId) {
        User user = userDbStorage.getUserById(userId);
        User friend = userDbStorage.getUserById(friendId);

        if (user != null && friend != null) {
            userDbStorage.removeFriend(user, friend);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<Set<User>> getFriendsList(@PathVariable Long userId) {
        User user = userDbStorage.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        Set<User> friendsList = userDbStorage.getFriendsList(user);
        return ResponseEntity.ok(friendsList);
    }

    @GetMapping("/{userId}/friends/common/{otherUserId}")
    public ResponseEntity<Set<User>> getMutualFriends(
            @PathVariable Long userId,
            @PathVariable Long otherUserId
    ) {
        User user = userDbStorage.getUserById(userId);
        User otherUser = userDbStorage.getUserById(otherUserId);

        if (user == null || otherUser == null) {
            return ResponseEntity.notFound().build();
        }

        Set<User> mutualFriends = userDbStorage.getListMutualFriends(user, otherUser);
        return ResponseEntity.ok(mutualFriends);
    }
}
