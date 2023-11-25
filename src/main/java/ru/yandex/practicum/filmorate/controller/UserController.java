package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int generatedId = 1;

    @GetMapping("/users")
    public List<User> findAll() {
        log.info("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @PostMapping(value = "/users")
    public User createUser(@RequestBody @Valid User user) {
        if (user.getLogin().contains(" ")) {
            log.error("Пользователь не прошёл валидацию.");
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        user.setId(generatedId++);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Добавлен новый пользователь");
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping(value = "/users")
    public User update(@RequestBody @Valid User user) {
        if (user.getLogin().contains(" ") || !users.containsKey(user.getId())) {
            log.error("Пользователь не прошёл валидацию.");
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Пользователь обновлён.");
        users.put(user.getId(), user);
        return user;
    }
}
