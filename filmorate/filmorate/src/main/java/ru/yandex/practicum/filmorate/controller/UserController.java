package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserService userService = new UserService();
    private List<User> users = new ArrayList<>();
    public int nextUserId = 1;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            user.setId(nextUserId++);
            userService.allUserValidate(user);
            users.add(user);
            log.info("Информация о созданных пользователях: {}", users);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании пользователя: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User updatedUser) {
        try {
            userService.allUserValidate(updatedUser);
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                if (user.getId() == id) {
                    updatedUser.setId(id);
                    users.set(i, updatedUser);
                    log.info("информация о обновлённых пользователях: {}", users);
                    return ResponseEntity.ok(updatedUser);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("колличество пользователей в текущий момент: {}", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
