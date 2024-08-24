package ru.yandex.practicum.filmorate.storage.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.LikeValidator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LikeDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final LikeValidator validate;

    public void like(Long id, Long userId) {
        validate.forLike(id, userId);
        String sql = "INSERT INTO likes (film_id, user_id) VALUES(?, ?)";
        jdbcTemplate.update(sql, id, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        validate.forRemove(id, userId);
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, id);
    }

    public List<Film> topFilms(int count) {
        validate.forTopFilms(count);
        String sql = "SELECT f.id, f.name, f.description, f.release, f.duration, f.rating_id " +
                "FROM films f " +
                "LEFT JOIN likes l ON l.film_id = f.id " +
                "GROUP BY f.id, f.name, f.description, f.release, f.duration, f.rating_id  " +
                "ORDER BY COUNT(1) DESC " +
                "LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper::mapRowToFilm, count);
        return films;
    }
}
