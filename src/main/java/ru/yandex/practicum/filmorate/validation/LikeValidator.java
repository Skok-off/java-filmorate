package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class LikeValidator {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public void forLike(Long filmId, Long userId) {
        validateFilmAndUserForLike(filmId, userId);
    }

    public void forRemove(Long filmId, Long userId) {
        validateFilmAndUserForLike(filmId, userId);
    }

    public void forTopFilms(int count) {
        if (count < 1) {
            throw new ValidationException("Количество запрашиваемых фильмов не может быть меньше 1.");
        }
    }

    private void validateFilmAndUserForLike(Long filmId, Long userId) {
        if (Objects.isNull(filmId)) {
            throw new ValidationException("Не указан id фильма.");
        }
        if (Objects.isNull(userId)) {
            throw new ValidationException("Не указан id пользователя.");
        }
        if (Objects.isNull(filmDbStorage.getFilm(filmId))) {
            throw new NotFoundException("Фильм не найден.");
        }
        if (Objects.isNull(userDbStorage.getUser(userId))) {
            throw new NotFoundException("Пользователь не найден.");
        }
    }

}
