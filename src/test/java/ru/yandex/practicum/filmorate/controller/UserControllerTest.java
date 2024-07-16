package ru.yandex.practicum.filmorate.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.helper.Constants;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    private UserController userController;

    @SneakyThrows
    private User getNewTestUser(Long id, String email, String login, String name, String birthday) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(birthday == null ? null : Constants.SIMPLE_DATE_FORMAT.parse(birthday));
        return user;
    }

    //копирую в новый экземпляр, чтобы не сравнивать с самим собой
    private User copyUser(User user) {
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setEmail(user.getEmail());
        newUser.setLogin(user.getLogin());
        newUser.setName(user.getName());
        newUser.setBirthday(user.getBirthday());
        return newUser;
    }

    @BeforeEach
    public void setUp() {
        userController = new UserController();
    }

    @Test
    void findAll() {
        Map<Long, User> expectedUsersMap = new HashMap<>();
        User user1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User user2 = getNewTestUser(2L, "user2@test.com", "login2", "name2", "2000-01-01");
        expectedUsersMap.put(user1.getId(), copyUser(user1));
        expectedUsersMap.put(user2.getId(), copyUser(user2));
        userController.create(user1);
        userController.create(user2);
        List<User> expectedFindAll = new ArrayList<>(expectedUsersMap.values());
        List<User> actualFindAll = new ArrayList<>(userController.findAll());
        assertEquals(expectedFindAll, actualFindAll, "Список пользователей не совпадает с ожидаемым");
    }

    @Test
    void create() {
        User expectedUser = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User actualUser = userController.create(copyUser(expectedUser));
        assertEquals(expectedUser, actualUser, "Тестовый пользователь не совпадает с созданным");
    }

    @Test
    void createDuplicateEmail() {
        User user1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User user2 = getNewTestUser(2L, "user1@test.com", "login2", "name2", "2000-01-01");
        userController.create(user1);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user2));
        assertEquals("POST Email уже используется.", exception.getMessage());
    }

    @Test
    void createNullEmail() {
        User user1 = getNewTestUser(1L, null, "login1", "name1", "1900-01-01");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("POST Email не указан.", exception.getMessage());
    }

    @Test
    void createNullLogin() {
        User user1 = getNewTestUser(1L, "user1@test.com", null, "name1", "1900-01-01");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("POST Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test
    void createLoginWithSpaces() {
        User user1 = getNewTestUser(1L, "user1@test.com", "lo gin", "name1", "1900-01-01");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("POST Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @Test
    void createFeatureBirthday() {
        User user1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "3000-01-01");
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("POST Некорректная дата рождения.", exception.getMessage());
    }

    @Test
    void createNullBirthday() {
        User user1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("POST Некорректная дата рождения.", exception.getMessage());
    }

    @Test
    void update() {
        User expectedUser = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        userController.create(expectedUser);
        expectedUser.setEmail("user1-updated@test.com");
        User actualUser = userController.update(copyUser(expectedUser));
        assertEquals(expectedUser, actualUser, "Тестовый пользователь не совпадает с созданным");
    }

    @Test
    void updateDuplicateEmail() {
        User user1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User user2 = getNewTestUser(2L, "user2@test.com", "login2", "name2", "2000-01-01");
        User user2Copy = copyUser(user2);
        user2Copy.setEmail(user1.getEmail());
        userController.create(user1);
        userController.create(user2);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.update(user2Copy));
        assertEquals("PUT Email уже используется.", exception.getMessage());
    }

    @Test
    void updateNullEmail() {
        User expectedUser = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User copy1 = copyUser(expectedUser);
        User copy2 = copyUser(expectedUser);
        copy2.setEmail(null);
        userController.create(copy1);
        User actualUser = userController.update(copy2);
        assertEquals(expectedUser, actualUser, "Email не должен измениться");
    }

    @Test
    void updateNullLogin() {
        User expectedUser = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User copy1 = copyUser(expectedUser);
        User copy2 = copyUser(expectedUser);
        copy2.setLogin(null);
        userController.create(copy1);
        User actualUser = userController.update(copy2);
        assertEquals(expectedUser, actualUser, "Логин не должен измениться");
    }

    @Test
    void updateLoginWithSpaces() {
        User copy1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "1900-01-01");
        User copy2 = copyUser(copy1);
        copy2.setLogin("lo gin");
        userController.create(copy1);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.update(copy2));
        assertEquals("PUT Логин не может быть пустым или содержать пробелы.", exception.getMessage());
    }

    @SneakyThrows
    @Test
    void updateFeatureBirthday() {
        User copy1 = getNewTestUser(1L, "user1@test.com", "login1", "name1", "2000-01-01");
        User copy2 = copyUser(copy1);
        copy2.setBirthday(Constants.SIMPLE_DATE_FORMAT.parse("3000-01-01"));
        userController.create(copy1);
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.update(copy2));
        assertEquals("PUT Некорректная дата рождения.", exception.getMessage());
    }

    @Test
    void updateNullBirthday() {
        User expectedUser = getNewTestUser(1L, "user1@test.com", "login1", "name1", "2000-01-01");
        User copy1 = copyUser(expectedUser);
        User copy2 = copyUser(expectedUser);
        copy2.setBirthday(null);
        userController.create(copy1);
        User actualUser = userController.update(copy2);
        assertEquals(expectedUser, actualUser, "Дата рождения не должна измениться");
    }
}