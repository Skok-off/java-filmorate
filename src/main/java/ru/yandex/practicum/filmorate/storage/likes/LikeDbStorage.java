package ru.yandex.practicum.filmorate.storage.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LikeDbStorage {
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final FilmDbStorage filmDbStorage;
    @Autowired
    private final UserDbStorage userDbStorage;
    @Autowired
    private final FilmMapper filmMapper;

    public void like(Long id, Long userId) {
        validateFilmAndUserForLike(id, userId);
        if (isExistsLike(id, userId)) throw new ValidationException("Лайк уже стоит.");
        String sql = "INSERT INTO likes (film_id, user_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        validateFilmAndUserForLike(id, userId);
        if (!isExistsLike(id, userId)) throw new ValidationException("Лайка и так нет.");
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, id);
    }

    private void validateFilmAndUserForLike(Long id, Long userId) {
        if (Objects.isNull(id)) throw new ValidationException("Не указан id фильма.");
        if (Objects.isNull(userId)) throw new ValidationException("Не указан id пользователя.");
        if (Objects.isNull(filmDbStorage.getFilm(id))) throw new NotFoundException("Фильм не найден.");
        if (Objects.isNull(userDbStorage.getUser(userId))) throw new NotFoundException("Пользователь не найден.");
    }

    public Collection<Film> topFilms(int count) {
        if (count < 1) throw new ValidationException("Количество запрашиваемых фильмов не может быть меньше 1.");
        String sql = "SELECT f.* FROM (SELECT COUNT(*) AS cnt_likes, l.film_id FROM likes l GROUP BY l.film_id) l JOIN films f ON f.id = l.film_id ORDER BY l.cnt_likes DESC LIMIT ?";
        return jdbcTemplate.query(sql, filmMapper::mapRowToFilm, count);
    }

    private boolean isExistsLike(Long filmId, Long userId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId, userId);
        return Objects.nonNull(count) && count > 0;
    }
}
