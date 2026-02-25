package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    @Builder.Default
    private Map<Long, FriendStatus> friendRequest = new HashMap<>();


    public User(long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friendsId = new HashSet<>();
    }

}
