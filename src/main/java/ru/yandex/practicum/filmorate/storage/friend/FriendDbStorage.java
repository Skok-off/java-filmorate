package ru.yandex.practicum.filmorate.storage.friend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.FriendValidator;

import java.util.Collection;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FriendDbStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FriendValidator validate;
    private final UserMapper userMapper;

    public void addFriend(Long userId, Long friendId) {
        validate.forAdd(userId, friendId);
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES(:userId, :friendId)";
        namedParameterJdbcTemplate.update(sql, Map.of("userId", userId, "friendId", friendId));
        log.info("Пользователи {} и {} подружились.", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        validate.forDelete(userId, friendId);
        String sql = "DELETE FROM friends WHERE user_id = :userId AND friend_id = :friendId";
        namedParameterJdbcTemplate.update(sql, Map.of("userId", userId, "friendId", friendId));
        log.info("Пользователи {} и {} больше не друзья.", userId, friendId);
    }

    public Collection<User> findFriends(Long id) {
        validate.forFind(id);
        String sql = "SELECT u.* " + "FROM users u " + "JOIN friends f ON f.friend_id = u.id WHERE f.user_id = :id ORDER BY u.id ";
        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), userMapper);
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        validate.forCommonFriends(userId, otherId);
        String sql = """
                    SELECT u.*
                    FROM users u
                    JOIN friends f1 ON f1.friend_id = u.id
                    JOIN friends f2 ON f2.friend_id = u.id
                    WHERE f1.user_id = :userId
                    AND f2.user_id = :otherId
                    ORDER BY u.id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("userId", userId, "otherId", otherId), userMapper);
    }

}
