package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.DirectorValidator;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
@Repository
public class DirectorDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final DirectorMapper mapper;
    private final DirectorValidator validator;

    public List<Director> findAll() {
        log.info("Запрошен список режиссеров");

        return jdbcTemplate.query("SELECT * FROM directors", mapper);
    }

    public Director findDirectorById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        log.info("Поиск режиссера по id {}", id);
        try {
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Режиссер с id %d не найден", id));
        }
    }

    public Director create(Director newDirector) {
        validator.forCreate(newDirector);
        String sql = "INSERT INTO directors (name) VALUES(?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[] {"id"});
            preparedStatement.setString(1, newDirector.getName());
            return preparedStatement;
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        Director director = findDirectorById(id);
        log.info(String.format("Добавлен режиссер %s с id = %d", director.getName(), director.getId()));

        return director;
    }

    public Director update(Director updatedDirector) {
        validator.forUpdate(updatedDirector);
        Long id = updatedDirector.getId();
        String sql = "UPDATE directors SET name = COALESCE(?, name) WHERE id = ?";
        jdbcTemplate.update(sql, updatedDirector.getName(), id);
        log.info("Обновлен режиссер с id = " + id);

        return findDirectorById(id);
    }

    public boolean deleteById(Long id) {
        log.info("Начало удаление режиссера по id - {}", id);

        return jdbcTemplate.update("DELETE FROM directors WHERE id = ?; ", id) > 0;
    }

    public List<Director> findFilmDirectors(Long id) {
        String sql = """
            SELECT d.* FROM directors d
            JOIN directors_films df
            ON d.id = df.director_id WHERE df.film_id = ?
            """;
        return jdbcTemplate.query(sql, mapper, id);
    }

    public void addDirectorsToFilm(Film film) {
        Long filmId = film.getId();

        String sql = "DELETE FROM directors_films WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);

        List<Director> directors = film.getDirectors();
        if (isEmpty(film.getDirectors())) {
            return;
        }
        Set<Long> directorsIds = directors.stream().map(Director::getId).collect(Collectors.toSet());

        checkDirectors(directorsIds);

        sql = "INSERT INTO directors_films (film_id, director_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, directorsIds, directorsIds.size(), (preparedStatement, id) -> {
            preparedStatement.setLong(1, filmId);
            preparedStatement.setLong(2, id);
        });

        film.setDirectors(findFilmDirectors(filmId));
    }

    public void checkDirectors(Set<Long> directorsIds) {
        if (isEmpty(directorsIds)) {
            throw new NotFoundException("У переданных режиссеров не указаны id");
        }

        String sql = "SELECT COUNT(*) FROM directors WHERE id IN (" + directorsIds.stream().map(id -> "?")
            .collect(Collectors.joining(",")) + ")";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, directorsIds.toArray());
        if (Objects.nonNull(count) && count != directorsIds.size()) {
            throw new ValidationException("Не все указанные режиссеры были найдены");
        }
    }

}
