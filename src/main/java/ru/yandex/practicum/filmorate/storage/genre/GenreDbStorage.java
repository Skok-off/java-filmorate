package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class GenreDbStorage {
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, GenreMapper::mapRowToGenre);
    }

    public Genre findGenre(Long id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, GenreMapper::mapRowToGenre, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Collection<Genre> findFilmGenres(Film film) {
        String sql = "SELECT g.* FROM genres g JOIN genres_films gf ON g.id = gf.genre_id WHERE gf.film_id = ?";
        return jdbcTemplate.query(sql, GenreMapper::mapRowToGenre, film.getId());
    }

    public void addGenresToFilm(Film film) {
        if (Objects.isNull(film.getGenres())) return;
        List<Genre> genres = film.getGenres();
        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        checkGenres(genreIds);
        String sql = "DELETE FROM genres_films WHERE film_id = ? AND genre_id NOT IN ("
                + genreIds.stream().map(id -> "?").collect(Collectors.joining(","))
                + ")";
        jdbcTemplate.update(sql, preparedStatement -> {
            preparedStatement.setLong(1, film.getId());
            int index = 2; // номера параметров для подстановки в запрос
            for (Long id : genreIds) {
                preparedStatement.setLong(index++, id);
            }
        });
        if (!genreIds.isEmpty()) {
            sql = "INSERT INTO genres_films (film_id, genre_id) VALUES (?, ?)";
            jdbcTemplate.batchUpdate(sql, genreIds, genreIds.size(), (preparedStatement, id) -> {
                preparedStatement.setLong(1, film.getId());
                preparedStatement.setLong(2, id);
            });
        }
        film.setGenres((List<Genre>) findFilmGenres(film));
    }

    public void checkGenres(Set<Long> genreIds) {
        if (genreIds.isEmpty()) return;
        String sql = "SELECT COUNT(*) FROM genres WHERE id IN ("
                + genreIds.stream().map(id -> "?").collect(Collectors.joining(","))
                + ")";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());
        if (Objects.nonNull(count) && count != genreIds.size()) {
            throw new ValidationException("Некоторых жанров не существует.");
        }
    }
}
