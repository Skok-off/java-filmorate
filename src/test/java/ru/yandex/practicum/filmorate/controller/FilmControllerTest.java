package ru.yandex.practicum.filmorate.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    @SneakyThrows
    private Film getNewTestFilm(Long id, String name, String description, String releaseDate, Integer duration) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate == null ? null : simpleDateFormat.parse(releaseDate));
        film.setDuration(duration);
        return film;
    }

    private Film copyFilm(Film film) {
        Film copy = new Film();
        copy.setId(film.getId());
        copy.setName(film.getName());
        copy.setDescription(film.getDescription());
        copy.setReleaseDate(film.getReleaseDate());
        copy.setDuration(film.getDuration());
        return copy;
    }

    @Test
    void findAll() {
        Film film1 = getNewTestFilm(1L, "film1", "description1", "2001-01-01", 101);
        Film film2 = getNewTestFilm(2L, "film2", "description2", "2002-01-01", 102);
        Map<Long, Film> testFilms = new HashMap<>();
        testFilms.put(film1.getId(), copyFilm(film1));
        testFilms.put(film2.getId(), copyFilm(film2));
        filmController.create(film1);
        filmController.create(film2);
        List<Film> expectedFilms = new ArrayList<>(testFilms.values());
        List<Film> actualFilms = new ArrayList<>(filmController.findAll());
        assertEquals(expectedFilms, actualFilms, "Списки добавленных фильмов должны совпадать");
    }

    @Test
    void create() {
        Film expectedFilm = getNewTestFilm(1L, "film1", "description1", "2001-01-01", 101);
        Film actualFilm = filmController.create(copyFilm(expectedFilm));
        assertEquals(expectedFilm, actualFilm, "Фильм не совпал с созданной по нему копией");
    }

    @Test
    void createNullName() {
        Film invalidFilm = getNewTestFilm(1L, null, "description1", "2001-01-01", 101);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Название фильма не может быть пустым", exception.getMessage());
    }

    @Test
    void createBlankName() {
        Film invalidFilm = getNewTestFilm(1L, "", "description1", "2001-01-01", 101);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Название фильма не может быть пустым", exception.getMessage());
    }

    @SneakyThrows
    @Test
    void createLongDescription() {
        Film invalidFilm = getNewTestFilm(1L, "film1", "description ".repeat(21), "2001-01-01", 101);
        Integer maxLength = (Integer) ReflectionTestUtils.getField(filmController, "MAX_DESCRIPTION_LENGTH");
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Длина описания не должна превышать " + maxLength + " символов", exception.getMessage());
    }

    @Test
    void createNullDescription() {
        String expectedDescription = "";
        Film film1 = getNewTestFilm(1L, "film1", null, "2001-01-01", 101);
        String actualDescription = filmController.create(film1).getDescription();
        assertEquals(expectedDescription, actualDescription, "Null в описании должен замениться на пустую строку");
    }

    @Test
    void createNullReleaseDate() {
        Film invalidFilm = getNewTestFilm(1L, "film1", "description", null, 101);
        String minDate = simpleDateFormat.format((Date) ReflectionTestUtils.getField(filmController, "MIN_RELEASE_DATE"));
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Дата релиза не должна быть раньше " + minDate, exception.getMessage());
    }

    @Test
    void createBeforeReleaseDate() {
        Date minDate = (Date) ReflectionTestUtils.getField(filmController, "MIN_RELEASE_DATE");
        Calendar calendar = Calendar.getInstance();
        assert minDate != null;
        calendar.setTime(minDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String invalidDate = simpleDateFormat.format(calendar.getTime());
        Film invalidFilm = getNewTestFilm(1L, "film1", "description", invalidDate, 101);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Дата релиза не должна быть раньше " + simpleDateFormat.format(minDate), exception.getMessage());
    }

    @Test
    void createNullDuration() {
        Film invalidFilm = getNewTestFilm(1L, "film1", "description", "2001-01-01", null);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void createNegativeDuration() {
        Film invalidFilm = getNewTestFilm(1L, "film1", "description", "2001-01-01", -1);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(invalidFilm));
        assertEquals("POST Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }

    @Test
    void update() {
        Film film1 = getNewTestFilm(1L, "film1", "description1", "2001-01-01", 101);
        Film expectedFilm = getNewTestFilm(1L, "film1updated", "description1updated", "2002-01-01", 102);
        filmController.create(film1);
        Film actualFilm = filmController.update(copyFilm(expectedFilm));
        assertEquals(expectedFilm, actualFilm, "Фильмы должны совпасть после обновления");
    }

    @Test
    void updateNotFoundFilm() {
        Film film1 = getNewTestFilm(1L, "film1", "description1", "2001-01-01", 101);
        Film film2 = getNewTestFilm(2L, "film2", "description2", "2002-01-01", 102);
        filmController.create(film1);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.update(film2));
        assertEquals("PUT Фильм с id = 2 не найден", exception.getMessage());
    }

    @Test
    void updateNullFields() {
        Film expectedFilm = getNewTestFilm(1L, "film1", "description1", "2001-01-01", 101);
        Film updateNulls = getNewTestFilm(1L, null, null, null, null);
        filmController.create(copyFilm(expectedFilm));
        Film actualFilm = filmController.update(copyFilm(updateNulls));
        assertEquals(expectedFilm, actualFilm, "Поля не должны обновиться на null");
    }
}