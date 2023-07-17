package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class FilmGenre {
    @Id
    private int id;

    private String name;

    public FilmGenre(int id) {
        this.id = id;
    }

    public FilmGenre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public FilmGenre() {
    }

}
