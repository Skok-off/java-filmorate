package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final InMemoryUserStorage inMemoryUserStorage;

    public Collection<User> findAll() {
        return inMemoryUserStorage.findAll();
    }

    public User create(User newUser) {
        return inMemoryUserStorage.create(newUser);
    }

    public User update(User newUser) {
        return inMemoryUserStorage.update(newUser);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя добавить в друзья самого себя.");
        User user = inMemoryUserStorage.getUser(userId);
        if (Objects.isNull(user)) throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        User friend = inMemoryUserStorage.getUser(friendId);
        if (Objects.isNull(friend)) throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        if (user.getFriends().contains(friendId))
            throw new ValidationException("Пользователи " + userId + " и " + friendId + " уже дружат.");
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} подружились.", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) throw new ValidationException("Нельзя удалять из друзей самого себя.");
        User user = inMemoryUserStorage.getUser(userId);
        if (Objects.isNull(user)) throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        User friend = inMemoryUserStorage.getUser(friendId);
        if (Objects.isNull(friend)) throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} больше не друзья.", userId, friendId);
    }

    public Collection<User> findFriends(Long id) {
        if (Objects.isNull(id)) throw new ValidationException("Не указан id.");
        if (Objects.isNull(inMemoryUserStorage.getUser(id))) throw new NotFoundException("Пользователь не найден.");
        return inMemoryUserStorage.getUser(id).getFriends().stream()
                .map(inMemoryUserStorage::getUser)
                .collect(Collectors.toSet());
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        if (Objects.isNull(userId) || Objects.isNull(otherId)) throw new ValidationException("Не указан id.");
        if (Objects.isNull(inMemoryUserStorage.getUser(userId)))
            throw new NotFoundException("Пользователь " + userId + " не найден.");
        if (Objects.isNull(inMemoryUserStorage.getUser(otherId)))
            throw new NotFoundException("Пользователь " + otherId + " не найден.");
        Set<Long> userFriends = inMemoryUserStorage.getUser(userId).getFriends();
        Set<Long> otherFriends = inMemoryUserStorage.getUser(otherId).getFriends();
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(inMemoryUserStorage::getUser)
                .collect(Collectors.toSet());
    }
}
