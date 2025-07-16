package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class User {
    private long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    @Builder.Default
    private Set<Long> friendsId = new HashSet<>();
}
