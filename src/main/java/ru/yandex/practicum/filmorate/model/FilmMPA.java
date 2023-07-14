package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class FilmMPA {
    @Id
    private Integer id;

    private String name;

    public FilmMPA() {
    }

    public FilmMPA(int id) {
        this.id = id;
    }

    public FilmMPA(int id, String rating) {
        this.id = id;
        this.name = rating;
    }
}
