package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.daoimpl.UserDbStorage;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validated.UserValidated;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserValidatedTest {
    UserController userController;
    UserService userService;
    UserDbStorage userDbStorage;
    private UserValidated userValidated;
    private User user;

    @BeforeEach
    public void setup() {
        DataSource dataSource = createDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDbStorage = new UserDbStorage(jdbcTemplate);
        userService = new UserService(userDbStorage);
        userController = new UserController(userService);
        userValidated = new UserValidated();
        user = new User();
    }

    private DataSource createDataSource() {
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
        String username = "sa";
        String password = "";
        String driverClassName = "org.h2.Driver";

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);

        return dataSource;
    }

    @Test
    public void testEmailValidation() {
        user.setEmail("");

        assertThrows(ValidationException.class, () -> {
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
