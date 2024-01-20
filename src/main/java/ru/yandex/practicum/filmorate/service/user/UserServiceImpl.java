package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserStorage;

import java.util.List;

@Slf4j
@Service
@Component
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User addUser(User user) {
        isUserValid(user);
        user = userStorage.addUser(user);
        log.info("Добавлен новый пользователь с ID = {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        isUserValid(user);
        int result = userStorage.updateUser(user);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.");
        }
        log.info("Пользователь с ID {} обновлён.", user.getId());
        return user;
    }

    @Override
    public User getUserById(int id) {
        List<User> users = userStorage.getUserById(id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        User user = users.get(0);
        log.info("Пользователь с id {} возвращён.", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userStorage.getAllUsers();
        log.info("Текущее количество пользователей: {}. Список возвращён.", users.size());
        return users;
    }

    @Override
    public void deleteUser(int id) {
        int result = userStorage.deleteUser(id);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        log.info("Пользователь с ID {} удалён.", id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        try {
            userStorage.addFriend(id, friendId);
            log.info("Пользователи с id {} добавил в друзья пользователя с id {}.", id, friendId);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        int result = userStorage.deleteFriend(id, friendId);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + id + " или с id " + friendId + " не найден.");
        }
        log.info("Пользователи с id {} удалил из друзей пользователя с id {}.", id, friendId);
    }

    @Override
    public List<User> getAllFriends(int id) {
        List<User> users = userStorage.getAllFriends(id);
        log.info("Список друзей пользователя с id {} возвращён.", id);
        return users;
    }

    @Override
    public List<User> getAllCommonFriends(int id, int otherId) {
        List<User> commonFriends = userStorage.getAllCommonFriends(id, otherId);
        log.info("Список общих друзей пользователей с id {} и с id {} возвращён.", id, otherId);
        return commonFriends;
    }

    private void isUserValid(User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Пользователь не прошёл валидацию.");
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}