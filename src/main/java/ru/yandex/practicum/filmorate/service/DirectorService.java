package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class DirectorService {

    private final DirectorDbStorage directorDbStorage;

    public List<Director> findAll() {
        return directorDbStorage.findAll();
    }

    public Director findDirectorById(Long id) {
        return directorDbStorage.findDirectorById(id);
    }

    public Director create(Director newDirector) {
        return directorDbStorage.create(newDirector);
    }

    public Director update(Director updatedDirector) {
        return directorDbStorage.update(updatedDirector);
    }

    public void deleteById(Long id) {
        directorDbStorage.deleteById(id);
    }

    //    public void like(Long id, Long userId) {
    //        likeDbStorage.like(id, userId);
    //    }
    //
    //    public void removeLike(Long id, Long userId) {
    //        likeDbStorage.removeLike(id, userId);
    //    }
    //
    //    public List<Film> topFilms(Long genreId, Integer year, int count) {
    //        return likeDbStorage.topFilms(genreId, year, count);
    //    }
    //
    //    public Collection<Film> getCommonPopularFilm(Long userId, Long friendId) {
    //        return filmDbStorage.getCommonPopularFilm(userId, friendId);
    //    }

}
