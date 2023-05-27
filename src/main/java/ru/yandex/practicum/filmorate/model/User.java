package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private int id;

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    private String name;

    @Email(message = "Некорректный формат электронной почты")
    private String email;

    @PastOrPresent(message = "Дата рождения не может быть пустой")
    private LocalDate birthday;

    @NonNull
    private Set<Long> friendsId = new HashSet<>();

    public User(int x, String email, String login, String name, LocalDate birthday) {
        this.id = x;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

}
