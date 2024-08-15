package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserValidator {
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public void forCreate(User newUser) {
        if (Objects.isNull(newUser.getEmail()) || newUser.getEmail().isBlank()) {
            throw new ValidationException(ErrorCode.NULL_OR_BLANK_EMAIL.getMessage());
        }
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, newUser.getEmail());
        if (Objects.nonNull(count) && count > 0) {
            throw new ValidationException(ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (Objects.isNull(newUser.getLogin()) || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
            throw new ValidationException(ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (Objects.isNull(newUser.getBirthday()) || newUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException(ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        if (Objects.isNull(newUser.getName()) || newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для " + newUser);
            newUser.setName(newUser.getLogin());
        }
    }

    public void forUpdate(User newUser) {
        if (Objects.isNull(newUser.getId())) {
            throw new ValidationException(ErrorCode.ID_IS_NULL.getMessage());
        }
        Long id = newUser.getId();
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        if (Objects.nonNull(count) && count == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        sql = "SELECT COUNT(*) FROM users WHERE id != ? AND email = ?";
        count = jdbcTemplate.queryForObject(sql, Integer.class, id, newUser.getEmail());
        if (Objects.nonNull(count) && count > 0) {
            throw new ValidationException(ErrorCode.DUPLICATE_EMAIL.getMessage());
        }
        if (Objects.nonNull(newUser.getLogin()) && (newUser.getLogin().isBlank() || newUser.getLogin().contains(" "))) {
            throw new ValidationException(ErrorCode.NULL_OR_BLANK_LOGIN.getMessage());
        }
        if (Objects.nonNull(newUser.getBirthday()) && newUser.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException(ErrorCode.INCORRECT_BIRTHDAY.getMessage());
        }
        if (Objects.nonNull(newUser.getName()) && newUser.getName().isBlank()) {
            log.info("Имя заменено на логин для id = " + id);
            newUser.setName(newUser.getLogin());
        }
    }
}
