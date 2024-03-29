package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Логин не может быть пустым")
    private String login;

    private String name;

    @Email(message = "Некорректный формат электронной почты")
    private String email;

    @PastOrPresent(message = "Дата рождения не может быть пустой")
    private LocalDate birthday;

    @ManyToMany
    @JoinTable(name = "Friend",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    private Set<Long> friendsId = new HashSet<>();

    private Set<Long> friendshipRequests = new HashSet<>();

    public User(int id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

}
