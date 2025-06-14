package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private int dynamicId = 1;
    Map<Integer, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@RequestBody User user) throws ValidateException {
        userValidate(user);
        user.setId(dynamicId++);
        users.put(user.getId(), user);
        log.info("Пользователь {} успешно создан", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) throws ValidateException {
        userValidate(user);
        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id {} не найден", user.getId());
            throw new ValidateException("Пользователь с id " + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("Пользователь с id {} обновлён", user.getId());
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        if (users.isEmpty()) {
            log.debug("нет активных пользователей");
        }

        return new ArrayList<>(users.values());
    }

    public void userValidate(User user) throws ValidateException {
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
