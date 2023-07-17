package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.daoimpl.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    UserService userService;
    UserDbStorage userDbStorage;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public UserController(@Qualifier("UserDbStorage") UserDbStorage userDbStorage) {
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

//    @PutMapping("/{id}/createFriendshipRequest/{friendId}")
//    public ResponseEntity<Void> createFriendshipRequest(@PathVariable("id") Long userId,
//                                                        @PathVariable("friendId") Long friendId) throws Exception {
//        User user = userDbStorage.getUserById(userId);
//        User friend = userDbStorage.getUserById(friendId);
//
//        userDbStorage.createFriendshipRequest(user, friend);
//
//        log.info("Friendship request created: User={}, Friend={}", user.getId(), friend.getId());
//
//        return ResponseEntity.ok().build();
//    }

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

    //    @PostMapping
//    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
//        try {
//            userService.addUser(user);
//            log.info("User added: {}", user);
//            return ResponseEntity.status(HttpStatus.CREATED).body(user);
//        } catch (InvalidLoginException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create user");
//        }
//    }
//
//    @PutMapping()
//    public ResponseEntity<?> updateUser(@RequestBody User user) {
//        try {
//            return userService.updateUser(user);
//        } catch (UserNotFoundException e) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
//        }
//    }
//
//    @PutMapping("/{id}/friends/{friendId}")
//    public ResponseEntity<Void> addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
//        User user = userService.getUserById(userId);
//        User friend = userService.getUserById(friendId);
//
//        if (user != null && friend != null) {
//            userService.addFriend(user, friend);
//            log.info("Друг добавлен: User={}, Friend={}", user, friend);
//            return ResponseEntity.ok().build();
//        } else {
//            log.warn("Пользователь или друг не найден: userId={}, friendId={}", userId, friendId);
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<?> getAllUsers() {
//        return userService.getAllUsers();
//    }
//
//    @GetMapping("/{id}/friends")
//    public ResponseEntity<Set<User>> getFriendsList(@PathVariable("id") Long userId) {
//        try {
//            User user = userService.getUserById(userId);
//            Set<User> friendsList = userService.getFriendsList(user);
//
//            List<User> sortedFriendsList = new ArrayList<>(friendsList);
//            sortedFriendsList.sort(Comparator.comparingLong(User::getId));
//
//            log.info("Friends list retrieved for user: User={}, Friends={}", user, sortedFriendsList);
//            return ResponseEntity.ok(new LinkedHashSet<>(sortedFriendsList));
//        } catch (UserNotFoundException e) {
//            log.warn("User not found: userId={}", userId);
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", e);
//        }
//    }
//
//
//    @GetMapping("/{id}/friends/common/{otherId}")
//    public ResponseEntity<Set<User>> getListMutualFriends(@PathVariable("id") Long userId,
//                                                          @PathVariable("otherId") Long otherId) {
//        User user = userService.getUserById(userId);
//        User otherUser = userService.getUserById(otherId);
//
//        if (user != null && otherUser != null) {
//            Set<User> mutualFriends = userService.getListMutualFriends(user, otherUser);
//            log.info("Mutual friends retrieved: User={}, OtherUser={}, MutualFriends={}",
//                    user, otherUser, mutualFriends);
//            return ResponseEntity.ok(mutualFriends);
//        } else {
//            log.warn("User(s) not found: userId={}, otherId={}", userId, otherId);
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    @GetMapping("/{userId}")
//    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
//        try {
//            User user = userService.getUserById(userId);
//            return ResponseEntity.ok(user);
//        } catch (UserNotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }
//
//
//    @DeleteMapping("/{id}/friends/{friendId}")
//    public ResponseEntity<?> removeFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
//        User user = userService.getUserById(userId);
//        User friend = userService.getUserById(friendId);
//
//        if (user != null && friend != null) {
//            userService.removeFriend(user, friend);
//            log.info("Friend removed: User={}, Friend={}", user, friend);
//            return ResponseEntity.ok().build();
//        } else {
//            log.warn("User or friend not found: userId={}, friendId={}", userId, friendId);
//            return ResponseEntity.notFound().build();
//        }
//    }

