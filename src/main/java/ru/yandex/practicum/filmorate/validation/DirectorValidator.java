package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class DirectorValidator {

    private final JdbcTemplate jdbcTemplate;

    public void forCreate(Director newDirector) {
        if (Objects.isNull(newDirector.getName()) || newDirector.getName().isBlank()) {
            log.info("Не указано имя режиссера");
            throw new ValidationException("Не указано имя режиссера");
        }
    }

    public void forUpdate(Director updatedDirector) {
        if (Objects.isNull(updatedDirector.getId())) {
            throw new ValidationException(ErrorCode.ID_IS_NULL.getMessage());
        }
        Long id = updatedDirector.getId();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM directors WHERE id = ?", Integer.class, id);
        if (Objects.nonNull(count) && count == 0) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
    }

}
