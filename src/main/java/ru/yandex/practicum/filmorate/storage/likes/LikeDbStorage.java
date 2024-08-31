package ru.yandex.practicum.filmorate.storage.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.LikeValidator;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LikeDbStorage {

    private final FilmMapper filmMapper;
    private final LikeValidator validate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    MapSqlParameterSource params;

    public void like(Long id, Long userId) {
        validate.forLike(id, userId);
        String sql = "MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES(:id, :userId)";
        namedParameterJdbcTemplate.update(sql, addParams(id, userId));
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        validate.forRemove(id, userId);
        String sql = "DELETE FROM likes WHERE film_id = :id AND user_id = :userId";
        namedParameterJdbcTemplate.update(sql, addParams(id, userId));
        log.info("Пользователь {} убрал лайк у фильма {}", userId, id);
    }

    public List<Film> topFilms(Long genreId, Integer year, int count) {
        params = new MapSqlParameterSource();
        validate.forTopFilms(count);
        log.info("Вывод популярных фильмов, с фильтрацией и без по жанру и годам genre_id {} и Year {} ", genreId, year);
        String sql =
                """
                                SELECT f.*, COUNT(DISTINCT l.user_id) AS cnt_likes
                                       FROM films f
                                       LEFT JOIN likes l on f.id = l.film_id
                                       LEFT JOIN genres_films gf on f.id = gf.film_id
                                       WHERE (gf.genre_id = :genreId OR :genreId IS NULL) AND (YEAR(f.release) = :year OR :year IS NULL)
                                       GROUP BY f.id
                                       ORDER BY cnt_likes DESC
                                       LIMIT :count
                        """;
        params.addValue("genreId", genreId);
        params.addValue("year", year);
        params.addValue("count", count);
        return namedParameterJdbcTemplate.query(sql, params, filmMapper);
    }

    private MapSqlParameterSource addParams(Long id, Long userId) {
        params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("userId", userId);

        return params;
    }
}

