package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class GenreDbStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GenreMapper mapper;

    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return namedParameterJdbcTemplate.query(sql, mapper);
    }

    public Genre findGenre(Long id) {
        String sql = "SELECT * FROM genres WHERE id = :id";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<Genre> findFilmGenres(Film film) {
        String sql = "SELECT g.* FROM genres g JOIN genres_films gf ON g.id = gf.genre_id WHERE gf.film_id = :id";
        return namedParameterJdbcTemplate.query(sql, Map.of("id", film.getId()), mapper);
    }

    public void addGenresToFilm(Film film) {
        if (Objects.isNull(film.getGenres())) {
            return;
        }
        List<Genre> genres = film.getGenres();
        Set<Long> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
        checkGenres(genreIds);
        String sql =
                """
                DELETE FROM genres_films WHERE film_id = :id AND genre_id NOT IN (:genreIds)
                """;
        Map<String, Object> deleteParams = new HashMap<>();
        deleteParams.put("id", film.getId());
        deleteParams.put("genreIds", genreIds);
        namedParameterJdbcTemplate.update(sql, deleteParams);

        if (!CollectionUtils.isEmpty(genreIds)) {
            String MergeSql =
                    """
                    MERGE INTO genres_films (film_id, genre_id) KEY (film_id, genre_id) VALUES (:id, :genreId)
                    """;
            MapSqlParameterSource mergeParams = new MapSqlParameterSource();
            mergeParams.addValue("id", film.getId());
            List<SqlParameterSource> batchParams = new ArrayList<>();

            for (Long genreId : genreIds) {
                mergeParams.addValue("genreId", genreId);
                batchParams.add(new MapSqlParameterSource(mergeParams.getValues()));
            }
            namedParameterJdbcTemplate.batchUpdate(MergeSql, batchParams.toArray(new SqlParameterSource[0]));
        }

        film.setGenres(findFilmGenres(film));
    }

    public void checkGenres(Set<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return;
        }
        String sql = "SELECT COUNT(*) FROM genres WHERE id IN (:genreIds)";

        Map<String, Object> params = new HashMap<>();
        params.put("genreIds", genreIds);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);

        if (Objects.nonNull(count) && count != genreIds.size()) {
            throw new ValidationException("Некоторых жанров не существует.");
        }
    }
}
