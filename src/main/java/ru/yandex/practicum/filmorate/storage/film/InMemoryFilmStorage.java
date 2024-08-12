package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.errors.ErrorCode;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.helper.Constants;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ru.yandex.practicum.filmorate.helper.Helper.getNextId;
import static ru.yandex.practicum.filmorate.helper.Helper.handleError;

@Slf4j
@Repository
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.info("Запрошен список фильмов");
        return films.values();
    }

    @Override
    public Film create(Film newFilm) {
        validateCreateFilm(newFilm);
        newFilm.setId(getNextId(films));
        newFilm.setDescription((Objects.isNull(newFilm.getDescription())) ? "" : newFilm.getDescription());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен фильм \"" + newFilm.getName() + "\" с id = " + newFilm.getId());
        return newFilm;
    }

    private void validateCreateFilm(Film film) {
        if (Objects.isNull(film.getName()) || film.getName().isBlank()) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (Objects.nonNull(film.getDescription()) && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            handleError("POST", ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (Objects.isNull(film.getReleaseDate()) || film.getReleaseDate().before(Constants.MIN_RELEASE_DATE)) {
            handleError("POST", ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (Objects.isNull(film.getDuration()) || film.getDuration() <= 0) {
            handleError("POST", ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
    }

    @Override
    public Film update(Film newFilm) {
        validateUpdateFilm(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName((Objects.isNull(newFilm.getName())) ? oldFilm.getName() : newFilm.getName());
        oldFilm.setDescription((Objects.isNull(newFilm.getDescription())) ? oldFilm.getDescription() : newFilm.getDescription());
        oldFilm.setReleaseDate((Objects.isNull(newFilm.getReleaseDate())) ? oldFilm.getReleaseDate() : newFilm.getReleaseDate());
        oldFilm.setDuration((Objects.isNull(newFilm.getDuration())) ? oldFilm.getDuration() : newFilm.getDuration());
        log.info("Обновлен фильм с id = " + newFilm.getId());
        return oldFilm;
    }

    private void validateUpdateFilm(Film film) {
        if (Objects.isNull(film.getId())) {
            handleError("PUT", ErrorCode.ID_IS_NULL.getMessage());
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        if (Objects.nonNull(film.getName()) && film.getName().isBlank()) {
            handleError("PUT", ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (Objects.nonNull(film.getDescription()) && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            handleError("PUT", ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (Objects.nonNull(film.getReleaseDate()) && film.getReleaseDate().before(Constants.MIN_RELEASE_DATE)) {
            handleError("PUT", ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (Objects.nonNull(film.getDuration()) && film.getDuration() <= 0) {
            handleError("PUT", ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
    }

    @Override
    public Film getFilm(Long id) {
        return films.getOrDefault(id, null);
    }
}
