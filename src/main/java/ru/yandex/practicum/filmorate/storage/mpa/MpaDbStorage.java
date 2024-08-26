package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaMapper mapper;

    public Mpa findMpa(Long id) {
        if (Objects.isNull(id)) {
            throw new ValidationException("Некорректный идентификатор рейтинга.");
        }
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM ratings ORDER BY id", mapper);
    }

}
