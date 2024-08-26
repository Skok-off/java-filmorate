package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@Component
@RequiredArgsConstructor
public final class FilmMapper implements RowMapper<Film> {

    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaDbStorage.findMpa(rs.getLong("rating_id")))
                .build();

        List<Genre> filmGenres = genreDbStorage.findFilmGenres(film);
        if (!isEmpty(filmGenres)) {
            film.setGenres(filmGenres);
        } else {
            film.setGenres(new ArrayList<>());
        }

        List<Director> filmDirectors = directorDbStorage.findFilmDirectors(film.getId());
        if (!isEmpty(filmDirectors)) {
            film.setDirectors(filmDirectors);
        } else {
            film.setDirectors(new ArrayList<>());
        }

        return film;
    }

}