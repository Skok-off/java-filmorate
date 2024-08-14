package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserDbStorage implements UserStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", UserMapper::mapRowToUser);
    }

    @Override
    public User create(User newUser) {
        validateCreateUser(newUser);
        String sql = "INSERT INTO users (name, login, email, birthday) VALUES(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, newUser.getName());
            preparedStatement.setString(2, newUser.getLogin());
            preparedStatement.setString(3, newUser.getEmail());
            preparedStatement.setDate(4, Date.valueOf(newUser.getBirthday()));
            return preparedStatement;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.info("Добавлен пользователь \"" + newUser.getName() + "\" с id = " + id);
        return newUser.toBuilder().id(id).build();
    }

    private void validateCreateUser(User newUser) {
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

    @Override
    public User update(User newUser) {
        validateUpdateUser(newUser);
        Long id = newUser.getId();
        String sql = "UPDATE users SET name = COALESCE(?, name), login = COALESCE(?, login), email = ?, birthday = COALESCE(?, birthday) WHERE id = ?";
        jdbcTemplate.update(sql, newUser.getName(), newUser.getLogin(), newUser.getEmail(), newUser.getBirthday(), id);
        log.info("Обновлен пользователь с id = " + id);
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
    }

    private void validateUpdateUser(User newUser) {
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

    @Override
    public User getUser(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
