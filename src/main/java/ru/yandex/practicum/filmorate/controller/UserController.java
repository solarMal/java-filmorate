package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.InvalidLoginException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collections;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
        try {
            userService.addUser(user);
            log.info("User added: {}", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (InvalidLoginException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user");
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try {
            return userService.updateUser(user);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {

        User user = userService.getUserById(userId);
        User friend = userService.getUserById(friendId);

        if (user != null && friend != null) {
            userService.addFriend(user, friend);
            log.info("Друг добавлен: User={}, Friend={}", user, friend);
        } else {
            log.warn("Пользователь или друг не найден: userId={}, friendId={}", userId, friendId);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}/friends")
    public Set<User> getFriendsList(@PathVariable("id") Long userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            Set<User> friendsList = userService.getFriendsList(user);
            log.info("Friends list retrieved for user: User={}, Friends={}", user, friendsList);
            return friendsList;
        } else {
            log.warn("User not found: userId={}", userId);
            return Collections.emptySet();
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getListMutualFriends(@PathVariable("id") Long userId,
                                          @PathVariable("otherId") Long otherId) {
        User user = userService.getUserById(userId);
        User otherUser = userService.getUserById(otherId);

        if (user != null && otherUser != null) {
            Set<User> mutualFriends = userService.getListMutualFriends(user, otherUser);
            log.info("Mutual friends retrieved: User={}, OtherUser={}, MutualFriends={}",
                    user, otherUser, mutualFriends);
            return mutualFriends;
        } else {
            log.warn("User(s) not found: userId={}, otherId={}", userId, otherId);
            return Collections.emptySet();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }


    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserById(userId);
        User friend = userService.getUserById(friendId);

        if (user != null && friend != null) {
            userService.removeFriend(user, friend);
            log.info("Friend removed: User={}, Friend={}", user, friend);
        } else {
            log.warn("User or friend not found: userId={}, friendId={}", userId, friendId);
        }
    }
}
