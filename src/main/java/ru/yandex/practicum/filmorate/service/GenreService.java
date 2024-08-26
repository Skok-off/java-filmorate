package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenreService {

    @Autowired
    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> findAll() {
        return genreDbStorage.findAll();
    }

    public Genre findGenre(Long id) {
        Genre genre = genreDbStorage.findGenre(id);
        if (Objects.isNull(genre)) {
            throw new NotFoundException("Жанр с id = " + id + " не найден.");
        }
        return genre;
    }

}
