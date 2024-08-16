package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class FriendValidator {
    @Autowired
    private final UserDbStorage userDbStorage;
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public void forAdd(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя добавить в друзья самого себя.");
        checkExists(userId);
        checkExists(friendId);
        if (isFriends(userId, friendId))
            throw new ValidationException("Пользователи " + userId + " и " + friendId + " уже дружат.");
    }

    public void forDelete(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя удалять из друзей самого себя.");
        checkExists(userId);
        checkExists(friendId);
    }

    public void forFind(Long id) {
        if (Objects.isNull(id)) throw new ValidationException("Не указан id.");
        checkExists(id);
    }

    public void forCommonFriends(Long userId, Long otherId) {
        if (Objects.isNull(userId) || Objects.isNull(otherId)) throw new ValidationException("Не указан id.");
        checkExists(userId);
        checkExists(otherId);
    }

    private boolean isFriends(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return Objects.nonNull(count) && count > 0;
    }

    private void checkExists(Long id) {
        if (Objects.isNull(userDbStorage.getUser(id)))
            throw new NotFoundException("Пользователь " + id + " не найден.");
    }
}
