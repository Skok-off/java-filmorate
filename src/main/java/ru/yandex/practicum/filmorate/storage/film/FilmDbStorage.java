package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final FilmMapper filmMapper;
    private final FilmValidator validate;

    @Override
    public List<Film> findAll() {
        log.info("Запрошен список фильмов");
        return jdbcTemplate.query("SELECT * FROM films ORDER BY id", filmMapper);
    }

    @Override
    public Film create(Film newFilm) {
        validate.forCreate(newFilm);
        String sql = "INSERT INTO films (name, description, release, duration, rating_id) VALUES(?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] {"id"});
            preparedStatement.setString(1, newFilm.getName());
            preparedStatement.setString(2, newFilm.getDescription());
            preparedStatement.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
            preparedStatement.setInt(4, newFilm.getDuration());
            preparedStatement.setLong(5, newFilm.getMpa().getId());
            return preparedStatement;
        }, keyHolder);
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
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        validate.forUpdate(newFilm);
        Long id = newFilm.getId();
        Mpa mpa = mpaDbStorage.findMpa(newFilm.getMpa().getId());
        String sql =
                "UPDATE films SET name = COALESCE(?, name), description = COALESCE(?, description), release = COALESCE(?, release), duration = COALESCE(?, duration), rating_id = COALESCE(?, rating_id) WHERE id = ?";
        jdbcTemplate.update(sql, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(), newFilm.getDuration(),
                Objects.isNull(mpa) ? null : mpa.getId(), id);
        genreDbStorage.addGenresToFilm(newFilm);
        directorDbStorage.addDirectorsToFilm(newFilm);
        log.info("Обновлен фильм с id = " + newFilm.getId());
        return jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", filmMapper::mapRowToFilm, id);
    }

    @Override
    public boolean deleteById(Long id) {
        try {
            boolean filmExists = filmExists(id);
            if (filmExists) {
                log.info("Начало удаление фильма - {}", id);
                return jdbcTemplate.update("DELETE FROM films WHERE id = ?; ", id) > 0;
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
            Film film = jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", filmMapper::mapRowToFilm, id);
            if (Objects.nonNull(film)) {
                film.setGenres((List<Genre>) genreDbStorage.findFilmGenres(film));
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
                                     WHERE fl.film_id IN (
                                         SELECT fl1.film_id
                                         FROM likes fl1
                                                  JOIN likes fl2 ON fl1.film_id = fl2.film_id
                                         WHERE fl1.user_id = ?
                                           AND fl2.user_id = ?
                                         )
                                     GROUP BY f.ID
                                     ORDER BY like_count DESC""";
        log.info("Поиск общих популярных фильмов пользователей с id {} и {} ", userId, friendId);

        List<Film> commonPopularFilms = jdbcTemplate.query(findCommonPopularFilms, filmMapper, userId, friendId);

        return commonPopularFilms.stream()
            .peek(film -> film.setGenres(genreDbStorage.findFilmGenres(film)))
            .peek(film -> film.setDirectors(directorDbStorage.findFilmDirectors(film.getId())))
            .collect(Collectors.toList());
    }

    @Override
    public boolean filmExists(Long filmId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM films WHERE id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, filmId));
    }

    public List<Film> getFilmsListByDirector(Long directorId, String sortBy) {
        String sql = "SELECT film_id FROM directors_films WHERE director_id = ?";
        List<Long> filmsIds = jdbcTemplate.queryForList(sql, Long.class, directorId);

        if (filmsIds.isEmpty()) {
            throw new NotFoundException("Не найдены фильмы, снятые этим режиссером");
        }

        switch (sortBy.toLowerCase()) {
            case "likes":
                sql = """
                    SELECT f.*, COUNT(l.user_id) likes_count 
                    FROM films f
                    JOIN likes l 
                    ON f.id = l.film_id
                    WHERE f.id IN (SELECT film_id FROM directors_films WHERE director_id = ?)
                    GROUP BY f.id
                    ORDER BY COUNT(likes_count)
                    """;
            case "year":
                sql = """
                    SELECT *
                    FROM films
                    WHERE id IN (SELECT film_id FROM directors_films WHERE director_id = ?)
                    ORDER BY YEAR(release)
                    """;

        }
        List<Film> sortedFilms = jdbcTemplate.query(sql, filmMapper, directorId);

        return sortedFilms.stream()
            .peek(film -> film.setGenres(genreDbStorage.findFilmGenres(film)))
            .peek(film -> film.setDirectors(directorDbStorage.findFilmDirectors(film.getId())))
            .collect(Collectors.toList());
    }

}
