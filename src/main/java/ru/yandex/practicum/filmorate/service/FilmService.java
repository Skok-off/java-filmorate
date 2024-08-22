package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikeDbStorage;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    @Autowired
    private final FilmDbStorage filmDbStorage;
    @Autowired
    private final LikeDbStorage likeDbStorage;
    @Autowired
    private final EventDbStorage eventDbStorage;

    public Collection<Film> findAll() {
        return filmDbStorage.findAll();
    }

    public Film findFilm(Long id) {
        return filmDbStorage.getFilm(id);
    }

    public Film create(Film newFilm) {
        return filmDbStorage.create(newFilm);
    }

    public Film update(Film newFilm) {
        return filmDbStorage.update(newFilm);
    }

    public void like(Long id, Long userId) {
        likeDbStorage.like(id, userId);
        eventDbStorage.add(userId, id, "film", "ADD", "LIKE");
    }

    public void removeLike(Long id, Long userId) {
        likeDbStorage.removeLike(id, userId);
    }

    public Collection<Film> topFilms(int count) {
        return likeDbStorage.topFilms(count);
    }
}
