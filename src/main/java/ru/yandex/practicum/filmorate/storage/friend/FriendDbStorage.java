package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FriendDbStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final UserDbStorage userDbStorage;

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя добавить в друзья самого себя.");
        User user = userDbStorage.getUser(userId);
        if (Objects.isNull(user)) throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        User friend = userDbStorage.getUser(friendId);
        if (Objects.isNull(friend)) throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        if (isFriends(userId, friendId))
            throw new ValidationException("Пользователи " + userId + " и " + friendId + " уже дружат.");
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователи {} и {} подружились.", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя удалять из друзей самого себя.");
        User user = userDbStorage.getUser(userId);
        if (Objects.isNull(user)) throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        User friend = userDbStorage.getUser(friendId);
        if (Objects.isNull(friend)) throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователи {} и {} больше не друзья.", userId, friendId);
    }

    public Collection<User> findFriends(Long id) {
        if (Objects.isNull(id)) throw new ValidationException("Не указан id.");
        if (Objects.isNull(userDbStorage.getUser(id))) throw new NotFoundException("Пользователь не найден.");
        String sql = "SELECT u.* " +
                "FROM users u " +
                "JOIN friends f ON f.friend_id = u.id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.id ";
        return jdbcTemplate.query(sql, UserMapper::mapRowToUser, id);
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        if (Objects.isNull(userId) || Objects.isNull(otherId)) throw new ValidationException("Не указан id.");
        if (Objects.isNull(userDbStorage.getUser(userId)))
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        if (Objects.isNull(userDbStorage.getUser(otherId)))
            throw new NotFoundException("Пользователь " + otherId + " не найден.");
        String sql = "SELECT u.* " +
                "FROM users u " +
                "JOIN friends f1 ON f1.friend_id = u.id " +
                "JOIN friends f2 ON f2.friend_id = u.id " +
                "WHERE f1.user_id = ? " +
                "AND f2.user_id = ? " +
                "ORDER BY u.id ";
        return jdbcTemplate.query(sql, UserMapper::mapRowToUser, userId, otherId);
    }

    private boolean isFriends(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return Objects.nonNull(count) && count > 0;
    }
}
