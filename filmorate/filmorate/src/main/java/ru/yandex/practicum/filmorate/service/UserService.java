package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public void emailValidation(User user){
        if (user.getEmail().isEmpty() || !user.getEmail().contains("@")){
            log.warn("электронная почта не может быть пустой и должна содержать символ @");
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
    }

    public void loginValidation(User user){
        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")){
            log.warn("логин не может быть пустым и содержать пробелы");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
    }

    public void nameCanBeEmptyButLoginWillBeUsed(User user){
        if (user.getName().isEmpty()){
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

    public void allUserValidate(User user){
        emailValidation(user);
        loginValidation(user);
        nameCanBeEmptyButLoginWillBeUsed(user);
        birthdayCannotBeInFuture(user);
    }
}
