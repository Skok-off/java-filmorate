package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.FriendValidator;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FriendDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendValidator validate;

    public void addFriend(Long userId, Long friendId) {
        validate.forAdd(userId, friendId);
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователи {} и {} подружились.", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validate.forDelete(userId, friendId);
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователи {} и {} больше не друзья.", userId, friendId);
    }

    public Collection<User> findFriends(Long id) {
        validate.forFind(id);
        String sql = "SELECT u.* " + "FROM users u " + "JOIN friends f ON f.friend_id = u.id WHERE f.user_id = ? ORDER BY u.id ";
        return jdbcTemplate.query(sql, UserMapper::mapRowToUser, id);
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        validate.forCommonFriends(userId, otherId);
        String sql =
            "SELECT u.* FROM users u JOIN friends f1 ON f1.friend_id = u.id JOIN friends f2 ON f2.friend_id = u.id WHERE f1.user_id = ? AND f2.user_id = ? ORDER BY u.id ";
        return jdbcTemplate.query(sql, UserMapper::mapRowToUser, userId, otherId);
    }

}
