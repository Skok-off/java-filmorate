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
        newFilm.setDescription((newFilm.getDescription() == null) ? "" : newFilm.getDescription());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен фильм \"" + newFilm.getName() + "\" с id = " + newFilm.getId());
        return newFilm;
    }

    private void validateCreateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            handleError("POST", ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (film.getDescription() != null && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            handleError("POST", ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().before(Constants.MIN_RELEASE_DATE)) {
            handleError("POST", ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            handleError("POST", ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
    }

    @Override
    public Film update(Film newFilm) {
        validateUpdateFilm(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName((newFilm.getName() == null) ? oldFilm.getName() : newFilm.getName());
        oldFilm.setDescription((newFilm.getDescription() == null) ? oldFilm.getDescription() : newFilm.getDescription());
        oldFilm.setReleaseDate((newFilm.getReleaseDate() == null) ? oldFilm.getReleaseDate() : newFilm.getReleaseDate());
        oldFilm.setDuration((newFilm.getDuration() == null) ? oldFilm.getDuration() : newFilm.getDuration());
        log.info("Обновлен фильм с id = " + newFilm.getId());
        return oldFilm;
    }

    private void validateUpdateFilm(Film film) {
        if (film.getId() == null) {
            handleError("PUT", ErrorCode.ID_IS_NULL.getMessage());
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        if (film.getName() != null && film.getName().isBlank()) {
            handleError("PUT", ErrorCode.NULL_OR_BLANK_NAME.getMessage());
        }
        if (film.getDescription() != null && film.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH) {
            handleError("PUT", ErrorCode.LONG_DESCRIPTION.getMessage());
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().before(Constants.MIN_RELEASE_DATE)) {
            handleError("PUT", ErrorCode.OLD_RELEASE_DATE.getMessage());
        }
        if (film.getDuration() != null && film.getDuration() <= 0) {
            handleError("PUT", ErrorCode.DURATION_NOT_POSITIVE.getMessage());
        }
    }

    @Override
    public Film getFilm(Long id) {
        return films.getOrDefault(id, null);
    }
}
