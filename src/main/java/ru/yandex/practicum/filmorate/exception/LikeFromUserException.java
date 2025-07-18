package ru.yandex.practicum.filmorate.exception;

public class LikeFromUserException extends RuntimeException {
    public LikeFromUserException(String message) {
        super(message);
    }
}
