package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserServiceImpl;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;
    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void setUp() {
        userController = new UserController(new UserServiceImpl(new InMemoryUserStorage()));
    }

    @Test
    @DisplayName("Успешное создание пользователя с ID 1")
    void shouldReturn1WhenValidUserCreate() {
        int expectedId = 1;
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name", birthday));

        assertEquals(expectedId, user.getId());
    }

    @Test
    @DisplayName("Успешное создание пользователя с ID 1 с пустым именем")
    void shouldReturn1WhenValidUserAndEmptyName() {
        int expectedId = 1;
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "", birthday));

        assertEquals(expectedId, user.getId());
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    @DisplayName("Успешное создание пользователя с ID 1 с нулловым именем")
    void shouldReturn1WhenValidUserAndNullName() {
        int expectedId = 1;
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", null, birthday));

        assertEquals(expectedId, user.getId());
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    @DisplayName("Возвращение списка пользователей")
    void shouldBeEqualsWhenValidUser() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = new User("mail@mail.ru", "dolore", "Nick Name", birthday);
        userController.createUser(user);
        List<User> userList = userController.findAllUsers();

        assertEquals(user, userList.get(0));
    }

    @Test
    @DisplayName("Обновление пользователя")
    void shouldBeNotEqualsWhenValidUserAndUpdate() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name", birthday));
        User updateUser = new User("mail@mail.ru", "dolore", "Update Name", birthday);
        updateUser.setId(user.getId());
        userController.updateUser(updateUser);
        List<User> userList = userController.findAllUsers();

        assertNotEquals(user, userList.get(0));
    }

    @Test
    @DisplayName("Пустое имя при бновление пользователя")
    void shouldBeNotEqualsWhenValidUserAndUpdateAndEmptyName() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name", birthday));
        User updateUser = new User("mail@mail.ru", "dolore", "", birthday);
        updateUser.setId(user.getId());
        userController.updateUser(updateUser);
        List<User> userList = userController.findAllUsers();

        assertNotEquals(user, userList.get(0));
    }

    @Test
    @DisplayName("Нулловаое имя при обновление пользователя")
    void shouldBeNotEqualsWhenValidUserAndUpdateAndNullName() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name", birthday));
        User updateUser = new User("mail@mail.ru", "dolore", null, birthday);
        updateUser.setId(user.getId());
        userController.updateUser(updateUser);
        List<User> userList = userController.findAllUsers();

        assertNotEquals(user, userList.get(0));
    }

    @Test
    @DisplayName("Неверный email")
    void shouldBeFalseWhenNotValidEmail() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        Set<ConstraintViolation<User>> violations = validator.validate(
                new User("mail", "dolore", "Nick Name", birthday));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Пустой логин")
    void shouldBeFalseWhenLoginIsBlank() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        Set<ConstraintViolation<User>> violations = validator.validate(
                new User("mail@mail.ru", "", "Nick Name", birthday));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("День рождения в будущем")
    void shouldBeFalseWhenWrongBirthday() {
        LocalDate birthday = LocalDate.of(2986, 10, 25);
        Set<ConstraintViolation<User>> violations = validator.validate(
                new User("mail@mail.ru", "dolore", "Nick Name", birthday));

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Логин содержит пробелы при создании пользователя")
    void shouldThrowExceptionWhenWrongLoginAndCreateUser() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.createUser(new User("mail@mail.ru", "dol ore", "Nick Name",
                        birthday)));

        assertEquals("Пользователь не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("Логин содержит пробелы при обновлении пользователя")
    void shouldThrowExceptionWhenWrongLoginAndUpdate() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        User user = userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name",
                birthday));
        User updateUser = new User("mail@mail.ru", "Wrong Login", "Nick Name", birthday);
        updateUser.setId(user.getId());
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.updateUser(updateUser));

        assertEquals("Пользователь не прошёл валидацию.", exception.getMessage());
    }

    @Test
    @DisplayName("Неправильный ID при обновлении пользователя")
    void shouldThrowExceptionWhenWrongIdAndUpdate() {
        LocalDate birthday = LocalDate.of(1986, 10, 25);
        userController.createUser(new User("mail@mail.ru", "dolore", "Nick Name",
                birthday));
        User updateUser = new User("mail@mail.ru", "Wrong Login", "Nick Name", birthday);
        updateUser.setId(999);
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userController.updateUser(updateUser));

        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }
}