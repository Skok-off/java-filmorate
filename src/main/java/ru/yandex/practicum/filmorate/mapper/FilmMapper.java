package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public final class FilmMapper  implements RowMapper<Film> {

    private final MpaDbStorage mpaDbStorage;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaDbStorage.findMpa(rs.getLong("rating_id")))
                .build();
    }

    public Film mapRowToFilm(ResultSet resultSet, int rowNum) throws SQLException {
        return Film.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(mpaDbStorage.findMpa(resultSet.getLong("rating_id")))
                .build();
    }
}