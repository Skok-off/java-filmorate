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

    public List<Film> topFilms(Long genreId, Integer year, int count) {
        validate.forTopFilms(count);
        log.info("Вывод популярных фильмов, с фильтрацией и без по жанру и годам genre_id {} и Year {} ", genreId, year);
        String sql =
            """
                        SELECT f.*, COUNT(DISTINCT l.user_id) AS cnt_likes
                               FROM films f
                               LEFT JOIN likes l on f.id = l.film_id
                               LEFT JOIN genres_films gf on f.id = gf.film_id
                               WHERE (gf.genre_id = ? OR ? IS NULL) AND (YEAR(f.release) = ? OR ? IS NULL)
                               GROUP BY f.id
                               ORDER BY cnt_likes DESC
                               LIMIT ?
                """;
        return jdbcTemplate.query(sql, filmMapper, genreId, genreId, year, year, count);
    }

}

