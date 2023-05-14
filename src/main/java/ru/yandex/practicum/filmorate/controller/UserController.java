package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserService userService = new UserService();
    private List<User> users = new ArrayList<>();
    public int nextUserId = 1;


    @PostMapping
    public ResponseEntity<?> createUser(@Validated @RequestBody User user) {
        try {
            user.setId(nextUserId++);
            userService.allUserValidate(user);
            if (users.contains(user)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(user);
            }
            users.add(user);
            log.info("Информация о созданных пользователях: {}", users);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании пользователя: {}", exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        try {
            int id = user.getId();
            userService.allUserValidate(user);
            if (users.size() == 0) {
//                user.setId(nextUserId++);
                users.add(user);
            } else {
                for (int i = 0; i < users.size(); i++) {
                    User existingUser = users.get(i);
                    if (existingUser.getId() == id) {
                        user.setId(id);
                        users.set(i, user);
                        log.info("Информация об обновленных пользователях: {}", users);
                        return ResponseEntity.ok(user);
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(user);
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
