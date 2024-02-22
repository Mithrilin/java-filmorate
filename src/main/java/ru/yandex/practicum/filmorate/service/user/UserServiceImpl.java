package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.event.EventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final int MAX_NEGATIVE_MARK_COUNT = 5;
    private static final double CORRECTION_COEFFICIENT = 10;
    private final UserDao userDao;
    private final FilmDao filmDao;
    private final EventService eventService;

    public UserServiceImpl(@Qualifier("userDbStorage") UserDao userStorage,
                           @Qualifier("filmDbStorage") FilmDao filmDao,
                           EventService eventService) {
        this.userDao = userStorage;
        this.eventService = eventService;
        this.filmDao = filmDao;
    }

    @Override
    public User addUser(User user) {
        isUserValid(user);
        user = userDao.addUser(user);
        log.info("Добавлен новый пользователь с ID = {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        isUserValid(user);
        int result = userDao.updateUser(user);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден.");
        }
        log.info("Пользователь с ID {} обновлён.", user.getId());
        return user;
    }

    @Override
    public User getUserById(int id) {
        List<User> users = userDao.getUserById(id);
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        User user = users.get(0);
        log.info("Пользователь с id {} возвращён.", user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userDao.getAllUsers();
        log.info("Текущее количество пользователей: {}. Список возвращён.", users.size());
        return users;
    }

    @Override
    public void deleteUser(int id) {
        int result = userDao.deleteUser(id);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + id + " не найден.");
        }
        log.info("Пользователь с ID {} удалён.", id);
    }

    @Override
    public void addFriend(int id, int friendId) {
        try {
            userDao.addFriend(id, friendId);
            log.info("Пользователи с id {} добавил в друзья пользователя с id {}.", id, friendId);
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException("Пользователь не найден.");
        }
        eventService.addEvent(new Event(id, "FRIEND", "ADD", friendId));
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        int result = userDao.deleteFriend(id, friendId);
        if (result == 0) {
            throw new NotFoundException("Пользователь с id " + id + " или с id " + friendId + " не найден.");
        }
        eventService.addEvent(new Event(id, "FRIEND", "REMOVE", friendId));
        log.info("Пользователи с id {} удалил из друзей пользователя с id {}.", id, friendId);
    }

    @Override
    public List<User> getAllFriends(int id) {
        userDao.getUserById(id).stream().findAny().orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id %d не найден", id)));
        List<User> users = userDao.getAllFriends(id);
        log.info("Список друзей пользователя с id {} возвращён.", id);
        return users;
    }

    @Override
    public List<User> getAllCommonFriends(int id, int otherId) {
        List<User> commonFriends = userDao.getAllCommonFriends(id, otherId);
        log.info("Список общих друзей пользователей с id {} и с id {} возвращён.", id, otherId);
        return commonFriends;
    }

    /**
     * Получение списка рекомендованных к просмотру фильмов.
     * Алгоритм определяет пользователя с наиболее похожими оценками, затем выбирает из списка положительно
     * оценённых фильмов те, которые не были оценены искомым пользователем.
     * Пользователь с наиболее похожими оценками определяются путём отношения суммы разниц всех оценок к одним и тем же
     * фильмам и корректирующего коэффициента к количеству оценок.
     *
     * @return список рекомендованных к просмотру фильмов.
     */
    @Override
    public List<Film> getRecommendations(int requesterId) {
        int limitCount = 5;
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark = userDao.getUserIdToFilmIdWithMark(requesterId);
        Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff = new HashMap<>();
        Map<Integer, Integer> userIdToMatch = new HashMap<>();
        calculateDifferencesAndMatchesBetweenUsers(userIdToFilmIdWithDiff,
                userIdToFilmIdWithMark,
                userIdToMatch,
                requesterId);
        Integer userIdWithMinDiff = getUserIdWithMinDiff(userIdToFilmIdWithDiff,
                userIdToFilmIdWithMark,
                userIdToMatch,
                requesterId);
        List<Film> recommendations;
        if (userIdWithMinDiff == null) {
            recommendations = filmDao.getPopularFilmsWithLimit(limitCount);
        } else {
            recommendations = userDao.getRecommendations(requesterId, userIdWithMinDiff);
        }
        log.info("Список рекомендаций для пользователя с id {} возвращён.", requesterId);
        return recommendations;
    }

    private void calculateDifferencesAndMatchesBetweenUsers(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                                            Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                                            Map<Integer, Integer> userIdToMatch,
                                                            int requesterId) {
        for (Map.Entry<Integer, HashMap<Integer, Integer>> users : userIdToFilmIdWithMark.entrySet()) {
            if (users.getKey() == requesterId) {
                continue;
            }
            enrichDifferencesAndMatches(users,
                    userIdToFilmIdWithDiff,
                    userIdToFilmIdWithMark,
                    userIdToMatch,
                    requesterId);
        }
    }

    private Integer getUserIdWithMinDiff(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                         Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                         Map<Integer, Integer> userIdToMatch,
                                         int requesterId) {
        double minDiffCount = Double.MAX_VALUE;
        Integer userIdWithMinDiff = null;
        for (Map.Entry<Integer, HashMap<Integer, Integer>> checkedUserIdToFilmIdWithDiff : userIdToFilmIdWithDiff.entrySet()) {
            int checkedUserId = checkedUserIdToFilmIdWithDiff.getKey();
            if (userIdToMatch.get(checkedUserId) == 0
                    || isUserHaveNotFilmsForRecommendations(userIdToFilmIdWithMark, requesterId, checkedUserId)) {
                continue;
            }
            int sumDiff = checkedUserIdToFilmIdWithDiff.getValue().values().stream().mapToInt(e -> e).sum();
            double diffCount = (sumDiff + CORRECTION_COEFFICIENT) / userIdToMatch.get(checkedUserId);

            if ((diffCount < minDiffCount)
                    || ((diffCount == minDiffCount) && (userIdToMatch.get(checkedUserId) > userIdToMatch.get(userIdWithMinDiff)))) {
                minDiffCount = diffCount;
                userIdWithMinDiff = checkedUserId;
            }
        }
        return userIdWithMinDiff;
    }

    private void enrichDifferencesAndMatches(Map.Entry<Integer, HashMap<Integer, Integer>> users,
                                             Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithDiff,
                                             Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                             Map<Integer, Integer> userIdToMatch,
                                             int requesterId) {
        for (Map.Entry<Integer, Integer> e : users.getValue().entrySet()) {
            int userId = users.getKey();
            if (!userIdToFilmIdWithDiff.containsKey(userId)) {
                userIdToFilmIdWithDiff.put(userId, new HashMap<>());
                userIdToMatch.put(userId, 0);
            }
            int filmId = e.getKey();
            int userMark = e.getValue();
            if (userIdToFilmIdWithMark.get(requesterId).containsKey(filmId)) {
                int requesterMark = userIdToFilmIdWithMark.get(requesterId).get(filmId);
                userIdToFilmIdWithDiff.get(userId).put(filmId, Math.abs(requesterMark - userMark));
                int newMatchCount = userIdToMatch.get(userId) + 1;
                userIdToMatch.put(userId, newMatchCount);
            }
        }
    }

    private boolean isUserHaveNotFilmsForRecommendations(Map<Integer, HashMap<Integer, Integer>> userIdToFilmIdWithMark,
                                                         int requesterId,
                                                         int checkedUserId) {
        Map<Integer, Integer> requesterMarksMap = userIdToFilmIdWithMark.get(requesterId);
        Map<Integer, Integer> checkedUserMarksMap = userIdToFilmIdWithMark.get(checkedUserId);
        List<Integer> filmsIdWithPositiveMark = checkedUserMarksMap.entrySet().stream()
                .filter(filmIdToMarkMap -> !requesterMarksMap.containsKey(filmIdToMarkMap.getKey())
                        && filmIdToMarkMap.getValue() > MAX_NEGATIVE_MARK_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return filmsIdWithPositiveMark.isEmpty();
    }

    @Override
    public List<Event> getEventsByUser(int userId) {
        getUserById(userId); // Проверка на наличие пользователя в базе
        return eventService.getUserEvents(userId);
    }

    // Метод проверки наличия пробела в логине и замены пустого имени на логин
    private void isUserValid(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Пользователь не прошёл валидацию.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}