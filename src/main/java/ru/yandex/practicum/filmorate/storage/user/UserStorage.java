package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    User getUserById(long id);

    List<User> getUsers();

    void deleteUserById(long id);

    void deleteAllUsers();

    void addFriend(long userId, long friendId);

    void addFriendRequest(long userId, long requesterId);

    void deleteFriendRequest(long userId, long requesterId);

    boolean isFriendRequestExists(long userId, long requesterId);

    void deleteFriend(long userId, long friendId);

    Set<Long> getFriendsId(long userId);
}
