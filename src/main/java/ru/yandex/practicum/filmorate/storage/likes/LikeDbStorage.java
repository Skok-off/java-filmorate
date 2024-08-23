package ru.yandex.practicum.filmorate.storage.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.LikeValidator;
import java.util.Collection;

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

    public Collection<Film> topFilms(int count) {
        validate.forTopFilms(count);
        String sql = "SELECT f.* FROM (SELECT COUNT(*) AS cnt_likes, l.film_id FROM likes l GROUP BY l.film_id) l JOIN films f ON f.id = l.film_id ORDER BY l.cnt_likes DESC LIMIT ?";
        return jdbcTemplate.query(sql, filmMapper::mapRowToFilm, count);
    }

}
