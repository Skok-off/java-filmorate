package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findDirector(@PathVariable Long id) {
        return directorService.findDirectorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        return directorService.update(director);
    }

    //    @PutMapping("/{id}/like/{userId}")
    //    public void like(@PathVariable Long id, @PathVariable Long userId) {
    //        filmService.like(id, userId);
    //    }

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable Long id) {
        directorService.deleteById(id);
    }

    //    @DeleteMapping("/{id}/like/{userId}")
    //    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
    //        filmService.removeLike(id, userId);
    //    }
    //
    //    @GetMapping("/popular")
    //    public List<Film> topFilms(@RequestParam(defaultValue = "10") int count,
    //                               @RequestParam(value = "genreId", required = false) Long genreId,
    //                               @RequestParam(value = "year", required = false) Integer year) {
    //        return filmService.topFilms(genreId, year, count);
    //    }
    //
    //    @GetMapping("/common")
    //    public Collection<Film> getCommonPopularFilms(@RequestParam Long userId, @RequestParam Long friendId) {
    //        return filmService.getCommonPopularFilm(userId, friendId);
    //    }

}