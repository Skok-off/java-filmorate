package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserDbStorage implements UserStorage {

    private final UserValidator validate;
    private final FilmMapper filmMapper;
    private final UserMapper userMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    MapSqlParameterSource params;

    @Override
    public Collection<User> findAll() {
        log.info("Запрошен список пользователей");
        return namedParameterJdbcTemplate.query("SELECT * FROM users ORDER BY id", userMapper);
    }

    @Override
    public User create(User newUser) {
        params = new MapSqlParameterSource();
        validate.forCreate(newUser);
        String sql = "INSERT INTO users (name, login, email, birthday) VALUES(:name, :login, :email, :birthday)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        params.addValue("name", newUser.getName());
        params.addValue("login", newUser.getLogin());
        params.addValue("email", newUser.getEmail());
        params.addValue("birthday", newUser.getBirthday());

        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.info("Добавлен пользователь \"" + newUser.getName() + "\" с id = " + id);
        return newUser.toBuilder().id(id).build();
    }

    @Override
    public User update(User newUser) {
        params = new MapSqlParameterSource();
        validate.forUpdate(newUser);
        Long id = newUser.getId();
        params.addValue("name", newUser.getName());
        params.addValue("login", newUser.getLogin());
        params.addValue("email", newUser.getEmail());
        params.addValue("birthday", newUser.getBirthday());
        params.addValue("id", id);
        String sql =
                "UPDATE users SET name = COALESCE(:name, name), login = COALESCE(:login, login), email = :email, birthday = COALESCE(:birthday, birthday) WHERE id = :id";
        namedParameterJdbcTemplate.update(sql, params);
        log.info("Обновлен пользователь с id = " + id);
        params = new MapSqlParameterSource();
        params.addValue("id", id);
        return namedParameterJdbcTemplate.queryForObject("SELECT * FROM users WHERE id = :id", params, userMapper);
    }

    @Override
    public User getUser(Long id) {
        params = new MapSqlParameterSource();
        try {
            params.addValue("id", id);
            return namedParameterJdbcTemplate.queryForObject("SELECT * FROM users WHERE id = :id", params, userMapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь " + id + " не найден");
        }
    }

    @Override
    public boolean deleteById(Long id) {
        params = new MapSqlParameterSource();
        log.info("Начало удаление пользователя - {}", id);
        try {
            boolean userExist = userExists(id);
            if (userExist) {
                log.debug("Успешное удаление пользователя");
                params.addValue("id", id);
                return namedParameterJdbcTemplate.update("DELETE FROM users WHERE id = :id", params) > 0;
            } else {
                throw new NotFoundException("Не содержит данного пользователя " + id);
            }
        } catch (RuntimeException e) {
            throw new NotFoundException("Пользователь " + id + " не найден");
        }
    }

    public boolean userExists(Long userId) {
        params = new MapSqlParameterSource();
        String sql = "SELECT EXISTS (SELECT 1 FROM users WHERE id = :userId)";
        params.addValue("userId", userId);
        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    public Collection<Film> getRecommendations(Long id) {
        params = new MapSqlParameterSource();
        String sql = "WITH similar_users AS ( " +
                "    SELECT ul2.user_id, " +
                "        COUNT(*) AS common_likes " +
                "    FROM likes ul1 " +
                "    JOIN likes ul2 ON ul1.film_id = ul2.film_id " +
                "    WHERE ul1.user_id = :id " +
                "        AND ul2.user_id != :id " +
                "    GROUP BY ul2.user_id " +
                "    ORDER BY common_likes DESC " +
                "    LIMIT 1 ) " +
                "SELECT f.id, f.name, f.description, f.release, f.duration, f.rating_id " +
                "FROM films f " +
                "JOIN likes l ON f.id = l.film_id " +
                "JOIN similar_users su ON l.user_id = su.user_id " +
                "WHERE f.id NOT IN (SELECT film_id FROM likes WHERE user_id = :id);";

        params.addValue("id", id);
        return namedParameterJdbcTemplate.query(sql, params, filmMapper);
    }
}
