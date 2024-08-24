package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final MpaDbStorage mpaDbStorage;
    @Autowired
    private final GenreDbStorage genreDbStorage;
    @Autowired
    private final FilmMapper filmMapper;
    @Autowired
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
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
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
        genreDbStorage.addGenresToFilm(film);
        log.info("Добавлен фильм \"" + film.getName() + "\" с id = " + id);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        validate.forUpdate(newFilm);
        Long id = newFilm.getId();
        Mpa mpa = mpaDbStorage.findMpa(newFilm.getMpa().getId());
        String sql = "UPDATE films SET name = COALESCE(?, name), description = COALESCE(?, description), release = COALESCE(?, release), duration = COALESCE(?, duration), rating_id = COALESCE(?, rating_id) WHERE id = ?";
        jdbcTemplate.update(sql, newFilm.getName(), newFilm.getDescription(), newFilm.getReleaseDate(), newFilm.getDuration(), Objects.isNull(mpa) ? null : mpa.getId(), id);
        genreDbStorage.addGenresToFilm(newFilm);
        log.info("Обновлен фильм с id = " + newFilm.getId());
        return jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", filmMapper::mapRowToFilm, id);
    }

    @Override
    public Film getFilm(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject("SELECT * FROM films WHERE id = ?", filmMapper::mapRowToFilm, id);
            if (Objects.nonNull(film)) film.setGenres((List<Genre>) genreDbStorage.findFilmGenres(film));
            return film;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
