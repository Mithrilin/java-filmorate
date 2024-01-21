package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDao {
    User addUser(User user);

    Integer updateUser(User user);

    List<User> getUserById(int id);

    List<User> getAllUsers();

    Integer deleteUser(int id);

    Integer addFriend(int id, int friendId);

    Integer deleteFriend(int id, int friendId);

    List<User> getAllFriends(int id);

    List<User> getAllCommonFriends(int id, int otherId);
}
