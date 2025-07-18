package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryUserStorageTest {
    InMemoryUserStorage userStorage;

    @BeforeEach
    void beforeEach() {
        userStorage = new InMemoryUserStorage();
    }

    @Test
    void validUserShouldPass() {
        User user = new User(1, "test@example.com", "login", "Имя", LocalDate.of(1990, 1, 1));
        assertDoesNotThrow(() -> userStorage.userValidate(user));
    }

    @Test
    void nullUserShouldThrowException() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> userStorage.userValidate(null));
        assertEquals("Пользователь не может быть null", ex.getMessage());
    }

    @Test
    void invalidEmailShouldThrow() {
        User user = new User(1, "invalid-email", "login", "Имя", LocalDate.of(1990, 1, 1));
        ValidateException ex = assertThrows(ValidateException.class, () -> userStorage.userValidate(user));
        assertEquals("Email не может быть пустым и должен содержать символ '@'", ex.getMessage());
    }

    @Test
    void blankLoginShouldThrow() {
        User user = new User(1, "test@example.com", " ", "Имя", LocalDate.of(1990, 1, 1));
        ValidateException ex = assertThrows(ValidateException.class, () -> userStorage.userValidate(user));
        assertEquals("Login не может быть пустым и не должен содержать пробелы", ex.getMessage());
    }

    @Test
    void nullNameShouldBeReplacedWithLogin() throws Exception {
        User user = new User(1, "test@example.com", "login", null, LocalDate.of(1990, 1, 1));
        userStorage.userValidate(user);
        assertEquals("login", user.getName());
    }

    @Test
    void futureBirthdayShouldThrow() {
        User user = new User(1, "test@example.com", "login", "Имя", LocalDate.now().plusDays(1));
        ValidateException ex = assertThrows(ValidateException.class, () -> userStorage.userValidate(user));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }
}
