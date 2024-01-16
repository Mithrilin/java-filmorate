package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int userId = 1;

    @Override
    public User addUser(User user) {
        user.setId(userId);
        userId++;
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Integer updateUser(User user) {
        users.put(user.getId(), user);
        return user.getId();
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public void addFriend(int id, int friendId) {
    }

    @Override
    public void deleteFriend(int id, int friendId) {
    }

    @Override
    public List<User> getAllFriends(int id) {
        return null;
    }

    @Override
    public List<User> getAllCommonFriends(int id, int otherId) {
        return null;
    }

    @Override
    public List<User> getUserById(int id) {
        List<User> user = new ArrayList<>();
        user.add(users.get(id));
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}