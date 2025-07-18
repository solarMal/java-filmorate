package ru.yandex.practicum.filmorate.exception;

public class FriendAlreadyExist extends RuntimeException {
    public FriendAlreadyExist(String message) {
        super(message);
    }
}
