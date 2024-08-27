package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class MpaDbStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaMapper mapper;

    public Mpa findMpa(Long id) {
        if (Objects.isNull(id)) {
            throw new ValidationException("Некорректный идентификатор рейтинга.");
        }
        String sql = "SELECT * FROM ratings WHERE id = :id";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Collection<Mpa> findAll() {
        return namedParameterJdbcTemplate.query("SELECT * FROM ratings ORDER BY id", mapper);
    }

}
