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
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserValidator validate;
    private final FilmMapper filmMapper;

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
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] {"id"});
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
        String sql =
            "UPDATE users SET name = COALESCE(?, name), login = COALESCE(?, login), email = ?, birthday = COALESCE(?, birthday) WHERE id = ?";
        jdbcTemplate.update(sql, newUser.getName(), newUser.getLogin(), newUser.getEmail(), newUser.getBirthday(), id);
        log.info("Обновлен пользователь с id = " + id);
        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
    }

    @Override
    public User getUser(Long id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", UserMapper::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь " + id + " не найден");
        }
    }

    @Override
    public boolean deleteById(Long id) {
        log.info("Начало удаление пользователя - {}", id);
        try {
            boolean userExist = userExists(id);
            if (userExist) {
                log.debug("Успешное удаление пользователя");
                return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id) > 0;
            } else {
                throw new NotFoundException("Не содержит данного пользователя " + id);
            }
        } catch (RuntimeException e) {
            throw new NotFoundException("Пользователь " + id + " не найден");
        }
    }

    public boolean userExists(Long userId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE id = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, userId);
    }

    public Collection<Film> getRecommendations(Long id) {
        String sql = "WITH similar_users AS ( " +
                "    SELECT ul2.user_id, " +
                "        COUNT(*) AS common_likes " +
                "    FROM likes ul1 " +
                "    JOIN likes ul2 ON ul1.film_id = ul2.film_id " +
                "    WHERE ul1.user_id = ? " +
                "        AND ul2.user_id != ? " +
                "    GROUP BY ul2.user_id " +
                "    ORDER BY common_likes DESC " +
                "    LIMIT 1 ) " +
                "SELECT f.id, f.name, f.description, f.release, f.duration, f.rating_id " +
                "FROM films f " +
                "JOIN likes l ON f.id = l.film_id " +
                "JOIN similar_users su ON l.user_id = su.user_id " +
                "WHERE f.id NOT IN (SELECT film_id FROM likes WHERE user_id = ?);";

        return jdbcTemplate.query(sql, filmMapper, id, id, id);
    }
}
