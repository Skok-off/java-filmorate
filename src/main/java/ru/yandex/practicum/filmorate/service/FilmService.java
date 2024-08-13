package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FilmService {
    private final InMemoryFilmStorage inMemoryFilmStorage;
    private final InMemoryUserStorage inMemoryUserStorage;

    public Collection<Film> findAll() {
        return inMemoryFilmStorage.findAll();
    }

    public Film create(Film newFilm) {
        return inMemoryFilmStorage.create(newFilm);
    }

    public Film update(Film newFilm) {
        return inMemoryFilmStorage.update(newFilm);
    }

    public void like(Long id, Long userId) {
        validateFilmAndUserForLike(id, userId);
        if (inMemoryFilmStorage.getFilm(id).getLikes().contains(userId))
            throw new ValidationException("Лайк уже стоит.");
        inMemoryFilmStorage.getFilm(id).getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, id);
    }

    public void removeLike(Long id, Long userId) {
        validateFilmAndUserForLike(id, userId);
        if (!inMemoryFilmStorage.getFilm(id).getLikes().contains(userId))
            throw new ValidationException("Лайка и так нет.");
        inMemoryFilmStorage.getFilm(id).getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, id);
    }

    private void validateFilmAndUserForLike(Long id, Long userId) {
        if (Objects.isNull(id)) throw new ValidationException("Не указан id фильма.");
        if (Objects.isNull(userId)) throw new ValidationException("Не указан id пользователя.");
        if (Objects.isNull(inMemoryFilmStorage.getFilm(id))) throw new NotFoundException("Фильм не найден.");
        if (Objects.isNull(inMemoryUserStorage.getUser(userId))) throw new NotFoundException("Пользователь не найден.");
    }

    public Collection<Film> topFilms(int count) {
        if (count < 1) throw new ValidationException("Количество запрашиваемых фильмов не может быть меньше 1.");
        return inMemoryFilmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(film -> ((Film) film).getLikes().size()).reversed())
                .collect(Collectors.toList());
    }
}
