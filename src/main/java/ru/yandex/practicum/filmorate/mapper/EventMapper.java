package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class EventMapper implements RowMapper<Event> {
    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .timestamp(rs.getTimestamp("datetime"))
                .userId(rs.getLong("user_id"))
                .eventType(rs.getString("et_name"))
                .operation(rs.getString("o_name"))
                .eventId(rs.getLong("id"))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}
