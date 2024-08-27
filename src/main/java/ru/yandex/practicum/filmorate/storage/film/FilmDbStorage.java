package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final FilmMapper filmMapper;
    private final FilmValidator validate;

    @Override
    public List<Film> findAll() {
        log.info("Запрошен список фильмов");
        return namedParameterJdbcTemplate.query("SELECT * FROM films", filmMapper);
    }

    @Override
    public Film create(Film newFilm) {
        validate.forCreate(newFilm);
        String sql = """
                INSERT INTO films (name, description, release, duration, rating_id)
                VALUES(:name, :description, :release, :duration, :ratingId)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", newFilm.getName())
                .addValue("description", newFilm.getDescription())
                .addValue("release", Date.valueOf(newFilm.getReleaseDate()))
                .addValue("duration", newFilm.getDuration())
                .addValue("ratingId", newFilm.getMpa().getId());
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        Mpa mpa = mpaDbStorage.findMpa(newFilm.getMpa().getId());
        Film film = newFilm.toBuilder().id(id).mpa(mpa).build();
        if (!isEmpty(newFilm.getGenres())) {
            genreDbStorage.addGenresToFilm(film);
        }
        if (!isEmpty(newFilm.getDirectors())) {
            directorDbStorage.addDirectorsToFilm(film);
        }
        log.info("Добавлен фильм \"" + film.getName() + "\" с id = " + id);
        return getFilm(film.getId());
    }

    @Override
    public Film update(Film newFilm) {
        validate.forUpdate(newFilm);
        Long id = newFilm.getId();
        Mpa mpa = mpaDbStorage.findMpa(newFilm.getMpa().getId());
        String sql = """
                    UPDATE films
                    SET name        = COALESCE(:name, name),
                        description = COALESCE(:description, description),
                        release     = COALESCE(:release, release),
                        duration    = COALESCE(:duration, duration),
                        rating_id   = COALESCE(:ratingId, rating_id)
                    WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", newFilm.getName())
                .addValue("description", newFilm.getDescription())
                .addValue("release", Date.valueOf(newFilm.getReleaseDate()))
                .addValue("duration", newFilm.getDuration())
                .addValue("ratingId", Objects.isNull(mpa) ? null : mpa.getId())
                .addValue("id", id);
        namedParameterJdbcTemplate.update(sql, params);
        genreDbStorage.addGenresToFilm(newFilm);
        directorDbStorage.addDirectorsToFilm(newFilm);
        log.info("Обновлен фильм с id = " + newFilm.getId());
        return namedParameterJdbcTemplate.queryForObject("SELECT * FROM films WHERE id = :id", Map.of("id", id), filmMapper);
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            boolean filmExists = filmExists(id);
            if (filmExists) {
                log.info("Начало удаление фильма - {}", id);
                return namedParameterJdbcTemplate.update("DELETE FROM films WHERE id = :id; ", Map.of("id", id)) > 0;
            } else {
                throw new NotFoundException("Не содержит данный фильм " + id);
            }
        } catch (RuntimeException e) {
            throw new NotFoundException("Фильм " + id + " не найден");
        }
    }

    @Override
    public Film getFilm(Long id) {
        try {
            Film film = namedParameterJdbcTemplate.queryForObject("SELECT * FROM films WHERE id = :id", Map.of("id", id), filmMapper);
            if (Objects.nonNull(film)) {
                film.setGenres(genreDbStorage.findFilmGenres(film));
                film.setDirectors(directorDbStorage.findFilmDirectors(id));
                film.setGenres(genreDbStorage.findFilmGenres(film));
            }
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм " + id + " не найден");
        }
    }

    public List<Film> getCommonPopularFilm(Long userId, Long friendId) {
        String findCommonPopularFilms = """
                SELECT f.*, r.name as rName, COUNT(fl.user_id) AS like_count
                FROM films f
                JOIN likes fl ON f.ID = fl.film_id
                JOIN ratings r on r.ID = f.rating_id
                WHERE fl.film_id IN (SELECT fl1.film_id
                                     FROM likes fl1
                                     JOIN likes fl2 ON fl1.film_id = fl2.film_id
                                     WHERE fl1.user_id = :userId
                                     AND fl2.user_id = :friendId)
                GROUP BY f.id
                ORDER BY like_count DESC
                """;
        log.info("Поиск общих популярных фильмов пользователей с id {} и {} ", userId, friendId);
        List<Film> commonPopularFilms = namedParameterJdbcTemplate.query(findCommonPopularFilms,
                Map.of("userId", userId,
                        "friendId", friendId),
                filmMapper);
        return commonPopularFilms.stream()
                .peek(film -> film.setGenres(genreDbStorage.findFilmGenres(film)))
                .peek(film -> film.setDirectors(directorDbStorage.findFilmDirectors(film.getId())))
                .collect(Collectors.toList());
    }

    public List<Film> search(String query, String by) {
        if (!by.equals("director") && !by.equals("title") && !by.equals("director,title") && !by.equals("title,director")) {
            log.error("Некорректные параметры запроса");
            throw new NotFoundException("Некорректные параметры запроса");
        }
        log.info("Вывод популярных фильмов, с учетом поиска по подстроке {} в поле таблицы фильмов {} ", query, by);
        String sql =
                """
                        SELECT f.*, COUNT(DISTINCT l.user_id) AS cnt_likes
                        FROM films f
                        LEFT JOIN likes l on f.id = l.film_id
                        LEFT JOIN directors_films df on f.id = df.film_id
                        LEFT JOIN directors d on df.director_id = d.id
                        WHERE (f.name ILIKE CONCAT('%', :query, '%') AND :by ILIKE '%title%')
                           OR (d.name ILIKE CONCAT('%', :query, '%') AND :by ILIKE '%director%')
                        GROUP BY f.id
                        ORDER BY cnt_likes DESC
                                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("query", query, "by", by), filmMapper);
    }

    @Override
    public boolean filmExists(Long filmId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM films WHERE id = :filmId)";
        return Boolean.TRUE.equals(namedParameterJdbcTemplate.queryForObject(sql, Map.of("filmId", filmId), Boolean.class));
    }

    public List<Film> getFilmsListByDirector(Long directorId, String sortBy) {
        String sql = "SELECT film_id FROM directors_films WHERE director_id = :directorId";
        List<Long> filmsIds = namedParameterJdbcTemplate.queryForList(sql, Map.of("directorId", directorId), Long.class);
        if (filmsIds.isEmpty()) {
            throw new NotFoundException("Не найдены фильмы, снятые этим режиссером");
        }
        sql = switch (sortBy.toLowerCase()) {
            case "likes" -> """
                    SELECT f.*, COUNT(l.user_id) likes_count
                    FROM films f
                    left JOIN likes l
                    ON f.id = l.film_id
                    WHERE f.id IN (SELECT film_id FROM directors_films WHERE director_id = :directorId)
                    GROUP BY f.id ORDER BY likes_count DESC
                    """;
            case "year" -> """
                    SELECT *
                    FROM films
                    WHERE id IN (SELECT film_id FROM directors_films WHERE director_id = :directorId)
                    ORDER BY YEAR(release)
                    """;
            default -> sql;
        };
        List<Film> sortedFilms = namedParameterJdbcTemplate.query(sql, Map.of("directorId", directorId), filmMapper);
        return sortedFilms.stream()
                .peek(film -> film.setGenres(genreDbStorage.findFilmGenres(film)))
                .peek(film -> film.setDirectors(directorDbStorage.findFilmDirectors(film.getId())))
                .collect(Collectors.toList());
    }

}
