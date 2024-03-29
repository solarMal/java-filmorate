package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validated.UserValidated;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserValidatedTest {
    UserController userController;
    UserService userService;
    InMemoryUserStorage inMemoryUserStorage;
    private UserValidated userValidated;
    private User user;


    @BeforeEach
    public void setup() {
        inMemoryUserStorage = new InMemoryUserStorage();
        userService = new UserService(inMemoryUserStorage);
        userController = new UserController(userService);
        userValidated = new UserValidated();
        user = new User();
        inMemoryUserStorage.nextUserId++;
    }

    @Test
    public void testEmailValidation() {
        user.setEmail("");

        Assertions.assertThrows(ValidationException.class, () -> {
            userValidated.emailValidation(user);
        });

        user.setEmail("invalidemail");

        Assertions.assertThrows(ValidationException.class, () -> {
            userValidated.emailValidation(user);
        });

        user.setEmail("invalid@@email.com");

        Assertions.assertThrows(ValidationException.class, () -> {
            userValidated.emailValidation(user);
        });

        user.setEmail("validemail@example.com");

        Assertions.assertDoesNotThrow(() -> {
            userValidated.emailValidation(user);
        });
    }

    @Test
    public void testEmailValidationInvalidFormat() {
        user.setEmail("invalidemail");

        assertThrows(ValidationException.class, () -> {
            userValidated.emailValidation(user);
        });
    }

    @Test
    public void testLoginValidation() {
        user.setLogin("");

        assertThrows(ValidationException.class, () -> {
            userValidated.loginValidation(user);
        });
    }

    @Test
    public void testLoginValidationContainsSpaces() {
        user.setLogin("user name");

        assertThrows(ValidationException.class, () -> {
            userValidated.loginValidation(user);
        });
    }

    @Test
    public void testNameCanBeEmptyButLoginWillBeUsed() {
        user.setLogin("username");
        user.setName(null);

        userValidated.nameCanBeEmptyButLoginWillBeUsed(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void testBirthdayCannotBeInFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        user.setBirthday(futureDate);

        assertThrows(ValidationException.class, () -> {
            userValidated.birthdayCannotBeInFuture(user);
        });
    }

    @Test
    public void testAllUserValidate() {
        user.setEmail("");
        user.setLogin("username");
        user.setName("");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> {
            userValidated.allUserValidate(user);
        });
    }
}
