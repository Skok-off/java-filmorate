package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;

public interface FilmStorage {

    Collection<Film> findAll();

    Film create(Film newFilm);

    Film update(Film newFilm);

    boolean deleteById(Long id);

    Film getFilm(Long id);

    boolean filmExists(Long filmId);

}
