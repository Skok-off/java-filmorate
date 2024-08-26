package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import java.util.List;

@SuppressWarnings("unused")
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

    @DeleteMapping("/{id}")
    public void deleteDirectorById(@PathVariable Long id) {
        directorService.deleteById(id);
    }

}