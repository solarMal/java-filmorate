package ru.yandex.practicum.filmorate.exceptions;

public class InvalidFriendshipRequestException extends RuntimeException{
    public InvalidFriendshipRequestException(String message) {
        super(message);
    }
}
