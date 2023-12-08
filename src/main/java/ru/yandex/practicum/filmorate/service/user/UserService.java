package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
    User addUser(User user);

    User updateUser(User user);

    void deleteUser(User user);

    List<User> getAllUsers();

    void addFriend(int id, int friendId);

    void deleteFriend(int id, int friendId);
}
