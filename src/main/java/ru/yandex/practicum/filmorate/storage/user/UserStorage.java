package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    void deleteUser(User user);

    Map<Integer, User> getAllUsers();
}
