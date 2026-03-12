package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Mpa {
    Long id;
    String name;

    public Mpa(Long id) {
        this.id = id;
    }

    public Mpa(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
