package ru.yandex.practicum.filmorate.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public User addUser(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Пользователь не прошёл валидацию.");
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Добавлен новый пользователь с ID = {}", user.getId());
        return userStorage.addUser(user);
    }

    @Override
    public User updateUser(User user) {
        List<User> users = userStorage.getAllUsers();
        if (user.getLogin().contains(" ") || !users.contains(user)) {
            log.error("Пользователь не прошёл валидацию.");
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Пользователь с ID {} обновлён.", user.getId());
        return userStorage.updateUser(user);
    }

    @Override
    public void deleteUser(User user) {
        log.info("Пользователь с ID {} удалён.", user.getId());
        userStorage.deleteUser(user);
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Текущее количество пользователей: {}", users.size());
        return users;
    }

    @Override
    public void addFriend(int id, int friendId) {
        log.info("Пользователи с id {} и с id {} стали друзьями.", id, friendId);
        userStorage.getAllUsers().get(id).getFriends().add(friendId);
        userStorage.getAllUsers().get(friendId).getFriends().add(id);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        User user = isIdValid(id);
        User friend = isIdValid(friendId);
        log.info("Пользователи с id {} и с id {} перестали быть друзьями.", id, friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }
}