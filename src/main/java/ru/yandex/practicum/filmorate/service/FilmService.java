package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.likes.LikeDbStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final LikeDbStorage likeDbStorage;
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

    public void deleteById(Long filmId) {
        filmDbStorage.deleteById(filmId);
    }

    public void like(Long id, Long userId) {
        likeDbStorage.like(id, userId);
        eventDbStorage.add(userId, id, "films", "ADD", "LIKE");
    }

    public void removeLike(Long id, Long userId) {
        likeDbStorage.removeLike(id, userId);
        eventDbStorage.add(userId, id, "films", "REMOVE", "LIKE");
    }

    public List<Film> topFilms(Long genreId, Integer year, int count) {
        return likeDbStorage.topFilms(genreId, year, count);
    }

    public Collection<Film> getCommonPopularFilm(Long userId, Long friendId) {
        return filmDbStorage.getCommonPopularFilm(userId, friendId);
    }

    public List<Film> getFilmsListByDirector(Long directorId, String sortBy) {
        return filmDbStorage.getFilmsListByDirector(directorId, sortBy);
    }

}
