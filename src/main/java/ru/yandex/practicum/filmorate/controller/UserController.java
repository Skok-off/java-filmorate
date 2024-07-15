package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.helper.Helper.getNextId;
import static ru.yandex.practicum.filmorate.helper.Helper.handleError;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();

    static {
        ERROR_MESSAGES.put("NullOrBlankLogin", "Логин не может быть пустым и содержать пробелы");
        ERROR_MESSAGES.put("IncorrectBirthday", "Дата рождения должна быть указана и не может быть в будущем");
        ERROR_MESSAGES.put("IdIsNull", "Не указан id пользователя");
        ERROR_MESSAGES.put("DuplicateEmail", "Данный email принадлежит другому пользователю");
        ERROR_MESSAGES.put("InvalidEmail", "Должен быть указан email");
    }

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        if (newUser.getEmail() == null || newUser.getEmail().isBlank()) {
            handleError(ERROR_MESSAGES, "InvalidEmail", "POST");
        }
        if (users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()))) {
            handleError(ERROR_MESSAGES, "DuplicateEmail", "POST");
        }
        if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            handleError(ERROR_MESSAGES, "NullOrBlankLogin", "POST");
        }
        if (newUser.getBirthday() == null || newUser.getBirthday().after(new Date())) {
            handleError(ERROR_MESSAGES, "IncorrectBirthday", "POST");
        }
        newUser.setId(getNextId(users));
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + newUser.getId());
            newUser.setName(newUser.getLogin());
        }
        users.put(newUser.getId(), newUser);
        log.info("Добавлен пользователь \"" + newUser.getName() + "\" с id = " + newUser.getId());
        return newUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            handleError(ERROR_MESSAGES, "IdIsNull", "PUT");
        }
        Long id = newUser.getId();
        if (!users.containsKey(id)) {
            handleError(ERROR_MESSAGES, "Пользователь с id = " + id + " не найден", "PUT");
        }
        if (newUser.getEmail() != null && users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()) && !Objects.equals(user.getId(), newUser.getId()))) {
            handleError(ERROR_MESSAGES, "DuplicateEmail", "PUT");
        }
        if (newUser.getLogin() != null && (newUser.getLogin().isBlank() || newUser.getLogin().contains(" "))) {
            handleError(ERROR_MESSAGES, "NullOrBlankLogin", "PUT");
        }
        if (newUser.getName() != null && newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + id);
            newUser.setName(newUser.getLogin());
        }
        if (newUser.getBirthday() != null && newUser.getBirthday().after(new Date())) {
            handleError(ERROR_MESSAGES, "IncorrectBirthday", "PUT");
        }
        User oldUser = users.get(id);
        oldUser.setEmail((newUser.getEmail() == null || newUser.getEmail().isBlank()) ? oldUser.getEmail() : newUser.getEmail());
        oldUser.setLogin((newUser.getLogin() == null) ? oldUser.getLogin() : newUser.getLogin());
        oldUser.setName((newUser.getName() == null) ? oldUser.getName() : newUser.getName());
        oldUser.setBirthday((newUser.getBirthday() == null) ? oldUser.getBirthday() : newUser.getBirthday());
        log.info("Обновлен пользователь с id = " + id);
        return oldUser;
    }
}
