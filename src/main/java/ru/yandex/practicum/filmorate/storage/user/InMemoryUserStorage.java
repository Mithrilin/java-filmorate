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
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(User user) {
        users.remove(user.getId());
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.of(users.get(id));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}