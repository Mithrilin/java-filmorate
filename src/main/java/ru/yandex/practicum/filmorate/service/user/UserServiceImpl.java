package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

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
        userStorage.updateUser(user);
        log.info("Пользователь с ID {} обновлён.", user.getId());
        return user;
    }

    @Override
    public User getUserById(int id) {
        List<User> users = userStorage.getUserById(id);
        if (users.isEmpty()) {
            throw new UserNotFoundException("Пользователь с id " + id + " не найден.");
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
        isIdValid(id);
        userStorage.deleteUser(id);
        log.info("Пользователь с ID {} удалён.", id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        User user = isIdValid(id);
        User friend = isIdValid(friendId);
        log.info("Пользователи с id {} и с id {} стали друзьями.", id, friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        User user = isIdValid(id);
        User friend = isIdValid(friendId);
        log.info("Пользователи с id {} и с id {} перестали быть друзьями.", id, friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    @Override
    public List<User> getAllFriends(int id) {
        User user = isIdValid(id);
        log.info("Список друзей пользователя с id {} возвращён.", id);
        return user.getFriends().stream()
                .map(friendId -> userStorage.getAllUsers().get(friendId))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getAllCommonFriends(int id, int otherId) {
        User user = isIdValid(id);
        User otherUser = isIdValid(otherId);
        log.info("Список общих друзей пользователей с id {} и с id {} возвращён.", id, otherId);
        return user.getFriends().stream()
                .filter(friendId -> otherUser.getFriends().contains(friendId))
                .map(otherFriendId -> userStorage.getAllUsers().get(otherFriendId))
                .collect(Collectors.toList());
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