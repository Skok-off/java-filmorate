package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import java.util.Collection;

public interface UserStorage {

    Collection<User> findAll();

    User create(User newUser);

    User update(User newUser);

    User getUser(Long id);

    boolean deleteById(Long id);

    boolean userExists(Long userId);

}
