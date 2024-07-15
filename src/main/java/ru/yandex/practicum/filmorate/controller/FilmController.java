package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.helper.Helper.getNextId;
import static ru.yandex.practicum.filmorate.helper.Helper.handleError;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final Integer MAX_DESCRIPTION_LENGTH = 200;
    private static final Date MIN_RELEASE_DATE;
    //вынес ошибки в список и вызов в отдельный метод, чтобы не дублировать текст в POST и PUT, в log и throw
    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();

    static {
        try {
            MIN_RELEASE_DATE = simpleDateFormat.parse("1895-12-28");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        ERROR_MESSAGES.put("NullOrBlankName", "Название фильма не может быть пустым");
        ERROR_MESSAGES.put("LongDescription", "Длина описания не должна превышать " + MAX_DESCRIPTION_LENGTH + " символов");
        ERROR_MESSAGES.put("OldReleaseDate", "Дата релиза не должна быть раньше " + simpleDateFormat.format(MIN_RELEASE_DATE));
        ERROR_MESSAGES.put("DurationIsNotPositive", "Продолжительность фильма должна быть положительным числом");
        ERROR_MESSAGES.put("IdIsNull", "Не указан id фильма");
    }

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрошен список фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        if (newFilm.getName() == null || newFilm.getName().isBlank()) {
            handleError(ERROR_MESSAGES, "NullOrBlankName", "POST");
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            handleError(ERROR_MESSAGES, "LongDescription", "POST");
        }
        if (newFilm.getReleaseDate() == null || newFilm.getReleaseDate().before(MIN_RELEASE_DATE)) {
            handleError(ERROR_MESSAGES, "OldReleaseDate", "POST");
        }
        if (newFilm.getDuration() == null || newFilm.getDuration() <= 0) {
            handleError(ERROR_MESSAGES, "DurationIsNotPositive", "POST");
        }
        newFilm.setId(getNextId(films));
        newFilm.setDescription((newFilm.getDescription() == null) ? "" : newFilm.getDescription());
        films.put(newFilm.getId(), newFilm);
        log.info("Добавлен фильм \"" + newFilm.getName() + "\" с id = " + newFilm.getId());
        return newFilm;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            handleError(ERROR_MESSAGES, "IdIsNull", "PUT");
        }
        Long id = newFilm.getId();
        if (!films.containsKey(id)) {
            handleError(ERROR_MESSAGES, "Фильм с id = " + id + " не найден", "PUT");
        }
        if (newFilm.getName() != null && newFilm.getName().isBlank()) {
            handleError(ERROR_MESSAGES, "NullOrBlankName", "PUT");
        }
        if (newFilm.getDescription() != null && newFilm.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            handleError(ERROR_MESSAGES, "LongDescription", "PUT");
        }
        if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().before(MIN_RELEASE_DATE)) {
            handleError(ERROR_MESSAGES, "OldReleaseDate", "PUT");
        }
        if (newFilm.getDuration() != null && newFilm.getDuration() <= 0) {
            handleError(ERROR_MESSAGES, "DurationIsNotPositive", "PUT");
        }
        Film oldFilm = films.get(id);
        oldFilm.setName((newFilm.getName() == null) ? oldFilm.getName() : newFilm.getName());
        oldFilm.setDescription((newFilm.getDescription() == null) ? oldFilm.getDescription() : newFilm.getDescription());
        oldFilm.setReleaseDate((newFilm.getReleaseDate() == null) ? oldFilm.getReleaseDate() : newFilm.getReleaseDate());
        oldFilm.setDuration((newFilm.getDuration() == null) ? oldFilm.getDuration() : newFilm.getDuration());
        log.info("Обновлен фильм с id = " + id);
        return oldFilm;
    }
}
