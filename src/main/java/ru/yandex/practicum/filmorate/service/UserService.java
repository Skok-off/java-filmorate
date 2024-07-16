package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.helper.Helper.getNextId;
import static ru.yandex.practicum.filmorate.helper.Helper.handleError;

@Slf4j
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return users.values();
    }

    public User create(User newUser) {
        validateCreateUser(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Добавлен пользователь \"" + newUser.getName() + "\" с id = " + newUser.getId());
        return newUser;
    }

    private void validateCreateUser(User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_EMAIL.getMessage());
        }
        if (users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()))) {
            handleError("POST", ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (newUser.getBirthday() == null || newUser.getBirthday().after(new Date())) {
            handleError("POST", ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        newUser.setId(getNextId(users));
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + newUser.getId());
            newUser.setName(newUser.getLogin());
        }
    }

    public User update(User newUser) {
        validateUpdateUser(newUser);
        User oldUser = users.get(newUser.getId());
        oldUser.setEmail((newUser.getEmail() == null || newUser.getEmail().isBlank()) ? oldUser.getEmail() : newUser.getEmail());
        oldUser.setLogin((newUser.getLogin() == null) ? oldUser.getLogin() : newUser.getLogin());
        oldUser.setName((newUser.getName() == null) ? oldUser.getName() : newUser.getName());
        oldUser.setBirthday((newUser.getBirthday() == null) ? oldUser.getBirthday() : newUser.getBirthday());
        log.info("Обновлен пользователь с id = " + newUser.getId());
        return oldUser;
    }

    private void validateUpdateUser(User newUser) {
        if (newUser.getId() == null) {
            handleError("PUT", ErrorCode.ID_IS_NULL.getMessage());
        }
        Long id = newUser.getId();
        if (!users.containsKey(id)) {
            handleError("PUT", "Пользователь с id = " + id + " не найден");
        }
        if (newUser.getEmail() != null && users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()) && !Objects.equals(user.getId(), newUser.getId()))) {
            handleError("PUT", ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (newUser.getLogin() != null && (newUser.getLogin().isBlank() || newUser.getLogin().contains(" "))) {
            handleError("PUT", ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (newUser.getBirthday() != null && newUser.getBirthday().after(new Date())) {
            handleError("PUT", ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        if (newUser.getName() != null && newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + id);
            newUser.setName(newUser.getLogin());
        }
    }
}
