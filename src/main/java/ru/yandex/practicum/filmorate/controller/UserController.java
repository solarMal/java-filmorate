package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exceptions.InvalidLoginException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
            userService.addUser(user);
            log.info("User added: {}", user);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody User user) {
            return userService.updateUser(user);
    }

    @PutMapping("/{id}/friend/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserById(userId);
        User friend = userService.getUserById(friendId);

        if (user != null && friend != null) {
            userService.addFriend(user, friend);
            log.info("Друг добавлен: User={}, Friend={}", user, friend);
            return ResponseEntity.ok().build();
        } else {
            log.warn("Пользователь или друг не найден: userId={}, friendId={}", userId, friendId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<User>> getFriendsList(@PathVariable("id") Long userId) {
        try {
            User user = userService.getUserById(userId);
            Set<User> friendsList = userService.getFriendsList(user);

            List<User> sortedFriendsList = new ArrayList<>(friendsList);
            sortedFriendsList.sort(Comparator.comparingLong(User::getId));

            log.info("Friends list retrieved for user: User={}, Friends={}", user, sortedFriendsList);
            return ResponseEntity.ok(new LinkedHashSet<>(sortedFriendsList));
        } catch (UserNotFoundException e) {
            log.warn("User not found: userId={}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
        }
    }


    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Set<User>> getListMutualFriends(@PathVariable("id") Long userId,
                                                          @PathVariable("otherId") Long otherId) {
        User user = userService.getUserById(userId);
        User otherUser = userService.getUserById(otherId);

        if (user != null && otherUser != null) {
            Set<User> mutualFriends = userService.getListMutualFriends(user, otherUser);
            log.info("Mutual friends retrieved: User={}, OtherUser={}, MutualFriends={}",
                    user, otherUser, mutualFriends);
            return ResponseEntity.ok(mutualFriends);
        } else {
            log.warn("User(s) not found: userId={}, otherId={}", userId, otherId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        User user = userService.getUserById(userId);
        User friend = userService.getUserById(friendId);

        if (user != null && friend != null) {
            userService.removeFriend(user, friend);
            log.info("Friend removed: User={}, Friend={}", user, friend);
            return ResponseEntity.ok().build();
        } else {
            log.warn("User or friend not found: userId={}, friendId={}", userId, friendId);
            return ResponseEntity.notFound().build();
        }
    }
}

