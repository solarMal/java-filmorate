package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.IncorrectParameterException;
import ru.yandex.practicum.filmorate.exceptions.UserAlreadyExistException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validated.UserValidated;

import java.util.HashSet;
import java.util.Set;

@Component
@Validated
public class InMemoryUserStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    UserValidated userValidated = new UserValidated();
    public Set<User> users = new HashSet<>();
    public int nextUserId = 1;

    @Override
    public ResponseEntity<User> createUser(User user) {
        try {
            user.setId(nextUserId++);
            userValidated.allUserValidate(user);
            if (users.contains(user)) {
                throw new UserAlreadyExistException("User already exists");
            }
            users.add(user);
            log.info("Информация о созданных пользователях: {}", users);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (ValidationException exception) {
            log.error("Ошибка при создании пользователя: {}", exception.getMessage());
            throw new IncorrectParameterException(exception.getMessage());
        }
    }


    @Override
    public ResponseEntity<?> updateUser(User user) {
        try {
            int id = user.getId();
            userValidated.allUserValidate(user);
            boolean userUpdated = false;
            for (User existingUser : users) {
                if (existingUser.getId() == id) {
                    if (user.getName().isEmpty()) {
                        user.setName(user.getLogin());
                    }
                    user.setId(id);
                    users.remove(existingUser);
                    users.add(user);
                    log.info("Информация об обновленных пользователях: {}", users);
                    userUpdated = true;
                    break;
                }
            }
            if (!userUpdated) {
                throw new UserNotFoundException("User not found" + HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.status(HttpStatus.OK).body(user);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Override
    public ResponseEntity<Set<User>> getAllUsers() {
        if (users.isEmpty()) {
            throw new UserNotFoundException("No users found" + HttpStatus.NOT_FOUND);
        }

        log.info("количество пользователей в текущий момент: {}", users.size());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Override
    public Set<User> getUsers(){
        return users;
    }
}
