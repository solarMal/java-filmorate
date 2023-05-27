package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest {
    UserController userController;
    private UserService userService;
    private User user;

    @BeforeEach
    public void setup() {
        userController = new UserController();
        userService = new UserService();
        user = new User();
        userController.nextUserId = 1;
    }

    @Test
    public void testEmailValidation() {
        user.setEmail("");

        assertThrows(ValidationException.class, () -> {
            userService.emailValidation(user);
        });
    }

    @Test
    public void testEmailValidationInvalidFormat() {
        user.setEmail("invalidemail");

        assertThrows(ValidationException.class, () -> {
            userService.emailValidation(user);
        });
    }

    @Test
    public void testLoginValidation() {
        user.setLogin("");

        assertThrows(ValidationException.class, () -> {
            userService.loginValidation(user);
        });
    }

    @Test
    public void testLoginValidationContainsSpaces() {
        user.setLogin("user name");

        assertThrows(ValidationException.class, () -> {
            userService.loginValidation(user);
        });
    }

    @Test
    public void testNameCanBeEmptyButLoginWillBeUsed() {
        user.setLogin("username");
        user.setName(null);

        userService.nameCanBeEmptyButLoginWillBeUsed(user);

        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void testBirthdayCannotBeInFuture() {
        LocalDate futureDate = LocalDate.now().plusDays(1);

        user.setBirthday(futureDate);

        assertThrows(ValidationException.class, () -> {
            userService.birthdayCannotBeInFuture(user);
        });
    }

    @Test
    public void testAllUserValidate() {
        user.setEmail("");
        user.setLogin("username");
        user.setName("");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> {
            userService.allUserValidate(user);
        });
    }
}
