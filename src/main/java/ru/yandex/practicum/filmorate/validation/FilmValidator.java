package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.helper.Constants;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class FilmValidator {

    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    public void forCreate(Film film) {
        if (Objects.isNull(film.getName()) || film.getName().isBlank()) {
            throw new ValidationException(ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (Objects.nonNull(film.getDescription()) && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException(ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (Objects.isNull(film.getReleaseDate()) || film.getReleaseDate().isBefore(Constants.MIN_RELEASE_DATE)) {
            throw new ValidationException(ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (Objects.isNull(film.getDuration()) || film.getDuration() <= 0) {
            throw new ValidationException(ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
        if (Objects.nonNull(film.getMpa().getName()) && (film.getMpa().getName().isBlank() || Objects.isNull(film.getMpa().getId()))) {
            throw new ValidationException("Указан некорректный рейтинг.");
        }
        if (Objects.isNull(mpaDbStorage.findMpa(film.getMpa().getId()))) {
            throw new ValidationException("Рейтинга МПА с id = " + film.getMpa().getId() + " нет");
        }
    }

    public void forUpdate(Film film) {
        if (Objects.isNull(film.getId())) {
            throw new ValidationException(ErrorCode.ID_IS_NULL.getMessage());
        }
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, film.getId());
        if (Objects.nonNull(count) && count == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        if (Objects.nonNull(film.getName()) && film.getName().isBlank()) {
            throw new ValidationException(ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (Objects.nonNull(film.getDescription()) && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException(ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (Objects.nonNull(film.getReleaseDate()) && film.getReleaseDate().isBefore(Constants.MIN_RELEASE_DATE)) {
            throw new ValidationException(ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (Objects.nonNull(film.getDuration()) && film.getDuration() <= 0) {
            throw new ValidationException(ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
    }

}
