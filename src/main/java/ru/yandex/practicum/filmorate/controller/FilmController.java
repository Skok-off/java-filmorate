package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Long id) {
        return filmService.findFilm(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Long id, @PathVariable Long userId) {
        filmService.like(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable Long id) {
        filmService.deleteById(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") int count,
                               @RequestParam(value = "genreId", required = false) Long genreId,
                               @RequestParam(value = "year", required = false) Integer year) {
        return filmService.topFilms(genreId, year, count);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonPopularFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonPopularFilm(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> search(@RequestParam @NotBlank String query,
                               @RequestParam(defaultValue = "title", required = false) String by) {
        log.info("Поиск по by= {} ", by);
        return filmService.search(query, by);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsListByDirectorSortedByLikesOrYear(@PathVariable Long directorId,
                                                                 @RequestParam String sortBy) {
        return filmService.getFilmsListByDirector(directorId, sortBy);
    }

}