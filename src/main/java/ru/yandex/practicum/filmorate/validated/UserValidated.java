package ru.yandex.practicum.filmorate.validated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Objects;

public class UserValidated {

    private static final Logger log = LoggerFactory.getLogger(UserValidated.class);

    public void emailValidation(User user) {
        String email = user.getEmail();
        if (email.isEmpty() || !email.contains("@") || countOccurrences(email, '@') > 1) {
            log.warn("Электронная почта не может быть пустой, должна содержать символ @ и не должна содержать более одного символа @");
            throw new ValidationException("Электронная почта не может быть пустой, должна содержать символ @ и не должна содержать более одного символа @");
        }
    }

    public void loginValidation(User user) {
        String login = user.getLogin();
        if (login == null || login.trim().isEmpty()) {
            log.warn("логин не может быть пустым");
            throw new ValidationException("логин не может быть пустым");
        }
        if (login.contains(" ")) {
            log.warn("логин не может содержать пробелы");
            throw new ValidationException("логин не может содержать пробелы");
        }
    }

    public void nameCanBeEmptyButLoginWillBeUsed(User user) {
        if (Objects.isNull(user.getName()) || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    public void birthdayCannotBeInFuture(User user) {
        LocalDate currentDate = LocalDate.now();
        LocalDate userBirthday = user.getBirthday();
        if (userBirthday.isAfter(currentDate)) {
            log.warn("Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private int countOccurrences(String text, char target) {
        int count = 0;
        for (char c : text.toCharArray()) {
            if (c == target) {
                count++;
            }
        }
        return count;
    }

    public void allUserValidate(User user) {
        emailValidation(user);
        loginValidation(user);
        nameCanBeEmptyButLoginWillBeUsed(user);
        birthdayCannotBeInFuture(user);
    }
}
