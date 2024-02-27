package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Map<Integer, HashMap<Integer, Integer>> getUserIdToFilmIdWithMark(int id);

    List<Film> getRecommendations(List<Integer> userIdWithMinDiff);

    Map<Integer, List<Genre>> getFilmIdToGenres(List<Integer> filmIdsForRecommendations);

    Map<Integer, List<Director>> getFilmIdToDirectors(List<Integer> filmIdsForRecommendations);
}
