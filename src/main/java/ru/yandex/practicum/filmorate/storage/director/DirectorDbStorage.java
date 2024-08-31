package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.DirectorValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
@Repository
public class DirectorDbStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DirectorMapper mapper;
    private final DirectorValidator validator;

    public List<Director> findAll() {
        log.info("Запрошен список режиссеров");
        return namedParameterJdbcTemplate.query("SELECT * FROM directors", mapper);
    }

    public Director findDirectorById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = :id";
        log.info("Поиск режиссера по id {}", id);
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), mapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Режиссер с id %d не найден", id));
        }
    }

    public Director create(Director newDirector) {
        validator.forCreate(newDirector);
        String sql = "INSERT INTO directors (name) VALUES(:name)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", newDirector.getName());
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        Director director = findDirectorById(id);
        log.info(String.format("Добавлен режиссер %s с id = %d", director.getName(), director.getId()));
        return director;
    }

    public Director update(Director updatedDirector) {
        validator.forUpdate(updatedDirector);
        Long id = updatedDirector.getId();
        String sql = "UPDATE directors SET name = COALESCE(:name, name) WHERE id = :id";
        namedParameterJdbcTemplate.update(sql, Map.of("name", updatedDirector.getName(), "id", id));
        log.info("Обновлен режиссер с id = " + id);
        return findDirectorById(id);
    }

    public boolean deleteById(Long id) {
        log.info("Начало удаление режиссера по id - {}", id);
        String sql = "DELETE FROM directors WHERE id = :id;";
        return namedParameterJdbcTemplate.update(sql, Map.of("id", id)) > 0;
    }

    public List<Director> findFilmDirectors(Long id) {
        String sql = """
                SELECT d.* FROM directors d
                JOIN directors_films df
                ON d.id = df.director_id WHERE df.film_id = :id
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("id", id), mapper);
    }

    public void addDirectorsToFilm(Film film) {
        Long filmId = film.getId();
        String sql = "DELETE FROM directors_films WHERE film_id = :filmId;";
        namedParameterJdbcTemplate.update(sql, Map.of("filmId", filmId));
        List<Director> directors = film.getDirectors();
        if (isEmpty(film.getDirectors())) {
            return;
        }
        Set<Long> directorsIds = directors.stream().map(Director::getId).collect(Collectors.toSet());
        checkDirectors(directorsIds);
        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        for (Long directorId : directorsIds) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("filmId", filmId);
            params.addValue("directorId", directorId);
            batchParams.add(params);
        }
        sql = "INSERT INTO directors_films (film_id, director_id) VALUES (:filmId, :directorId)";
        namedParameterJdbcTemplate.batchUpdate(sql, batchParams.toArray(new MapSqlParameterSource[0]));
        film.setDirectors(findFilmDirectors(filmId));
    }

    public void checkDirectors(Set<Long> directorsIds) {
        if (isEmpty(directorsIds)) {
            throw new NotFoundException("У переданных режиссеров не указаны id");
        }
        String sql = "SELECT COUNT(*) FROM directors WHERE id IN (:ids)";
        Integer count = namedParameterJdbcTemplate.queryForObject(sql, Map.of("ids", directorsIds), Integer.class);
        if (Objects.nonNull(count) && count != directorsIds.size()) {
            throw new ValidationException("Не все указанные режиссеры были найдены");
        }
    }

}
