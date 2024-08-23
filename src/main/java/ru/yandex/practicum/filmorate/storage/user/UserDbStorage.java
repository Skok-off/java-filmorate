package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserDbStorage implements UserStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final UserValidator validate;

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", UserMapper::mapRowToUser);
    }

    @Override
    public User create(User newUser) {
        validate.forCreate(newUser);
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

    @Override
    public User update(User newUser) {
        validate.forUpdate(newUser);
        Long id = newUser.getId();
        String sql = "UPDATE users SET name = COALESCE(?, name), login = COALESCE(?, login), email = ?, birthday = COALESCE(?, birthday) WHERE id = ?";
        jdbcTemplate.update(sql, newUser.getName(), newUser.getLogin(), newUser.getEmail(), newUser.getBirthday(), id);
        log.info("Обновлен пользователь с id = " + id);
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
    }

    @Override
    public User getUser(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }
}
