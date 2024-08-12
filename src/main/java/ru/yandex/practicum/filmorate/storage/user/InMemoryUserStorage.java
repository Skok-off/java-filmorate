package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

import static ru.yandex.practicum.filmorate.helper.Helper.getNextId;
import static ru.yandex.practicum.filmorate.helper.Helper.handleError;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return users.values();
    }

    @Override
    public User create(User newUser) {
        validateCreateUser(newUser);
        users.put(newUser.getId(), newUser);
        log.info("Добавлен пользователь \"" + newUser.getName() + "\" с id = " + newUser.getId());
        return newUser;
    }

    private void validateCreateUser(User newUser) {
        if (Objects.isNull(newUser.getEmail()) || newUser.getEmail().isBlank()) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_EMAIL.getMessage());
        }
        if (users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()))) {
            handleError("POST", ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (Objects.isNull(newUser.getLogin()) || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (Objects.isNull(newUser.getBirthday()) || newUser.getBirthday().after(new Date())) {
            handleError("POST", ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        newUser.setId(getNextId(users));
        if (Objects.isNull(newUser.getName()) || newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + newUser.getId());
            newUser.setName(newUser.getLogin());
        }
    }

    @Override
    public User update(User newUser) {
        validateUpdateUser(newUser);
        User oldUser = users.get(newUser.getId());
        oldUser.setEmail((Objects.isNull(newUser.getEmail()) || newUser.getEmail().isBlank()) ? oldUser.getEmail() : newUser.getEmail());
        oldUser.setLogin((Objects.isNull(newUser.getLogin())) ? oldUser.getLogin() : newUser.getLogin());
        oldUser.setName((Objects.isNull(newUser.getName())) ? oldUser.getName() : newUser.getName());
        oldUser.setBirthday((Objects.isNull(newUser.getBirthday())) ? oldUser.getBirthday() : newUser.getBirthday());
        log.info("Обновлен пользователь с id = " + newUser.getId());
        return oldUser;
    }

    private void validateUpdateUser(User newUser) {
        if (Objects.isNull(newUser.getId())) {
            handleError("PUT", ErrorCode.ID_IS_NULL.getMessage());
        }
        Long id = newUser.getId();
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        if (Objects.nonNull(newUser.getEmail()) && users.values().stream()
                .anyMatch(user -> user.getEmail().equals(newUser.getEmail()) && !Objects.equals(user.getId(), newUser.getId()))) {
            handleError("PUT", ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (Objects.nonNull(newUser.getLogin()) && (newUser.getLogin().isBlank() || newUser.getLogin().contains(" "))) {
            handleError("PUT", ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (Objects.nonNull(newUser.getBirthday()) && newUser.getBirthday().after(new Date())) {
            handleError("PUT", ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        if (Objects.nonNull(newUser.getName()) && newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + id);
            newUser.setName(newUser.getLogin());
        }
    }

    @Override
    public User getUser(Long id) {
        return users.getOrDefault(id, null);
    }
}
