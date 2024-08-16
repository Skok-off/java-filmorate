package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class LikeValidator {
    @Autowired
    private final FilmDbStorage filmDbStorage;
    @Autowired
    private final UserDbStorage userDbStorage;
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public void forLike(Long filmId, Long userId) {
        validateFilmAndUserForLike(filmId, userId);
        if (isExistsLike(filmId, userId)) throw new ValidationException("Лайк уже стоит.");
    }

    public void forRemove(Long filmId, Long userId) {
        validateFilmAndUserForLike(filmId, userId);
        if (!isExistsLike(filmId, userId)) throw new ValidationException("Лайка и так нет.");
    }

    public void forTopFilms(int count) {
        if (count < 1) throw new ValidationException("Количество запрашиваемых фильмов не может быть меньше 1.");
    }

    private void validateFilmAndUserForLike(Long filmId, Long userId) {
        if (Objects.isNull(filmId)) throw new ValidationException("Не указан id фильма.");
        if (Objects.isNull(userId)) throw new ValidationException("Не указан id пользователя.");
        if (Objects.isNull(filmDbStorage.getFilm(filmId))) throw new NotFoundException("Фильм не найден.");
        if (Objects.isNull(userDbStorage.getUser(userId))) throw new NotFoundException("Пользователь не найден.");
    }

    private boolean isExistsLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return Objects.nonNull(count) && count > 0;
    }
}
