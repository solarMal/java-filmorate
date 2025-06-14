package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class User {
    private int id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}
