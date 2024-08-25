package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EventDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserDbStorage userDbStorage;
    private final EventMapper mapper;

    public List<Event> getUserEvents(Long userId) {
        userDbStorage.getUser(userId);
        String sql = """
                SELECT e.datetime, e.user_id, et.name AS et_name, o.name AS o_name, e.id, e.entity_id
                FROM events e
                JOIN operations o ON e.operation_id = o.id
                JOIN event_types et ON e.event_type_id = et.id
                WHERE e.user_id = ?
                ORDER BY e.datetime;
                """;
        return jdbcTemplate.query(sql, mapper, userId);
    }

    public void add(Long userId, Long entityId, String entityType, String operation, String eventType) {
        Long operationId = getOperationIdByName(operation);
        Long eventTypeId = getEventTypeIdByName(eventType);
        Long entityTypeId = getEntityTypeIdByName(entityType);
        String sql = "INSERT INTO events (user_id, entity_id, entity_type_id, operation_id, event_type_id)" +
                "VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, entityId, entityTypeId, operationId, eventTypeId);
    }

    private Long getOperationIdByName(String operation) {
        String sql = "SELECT id FROM operations WHERE name = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, operation);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Операция \"" + operation + "\" не найдена.");
        }
    }

    private Long getEventTypeIdByName(String eventType) {
        String sql = "SELECT id FROM event_types WHERE name = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, eventType);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Тип события \"" + eventType + "\" не найден.");
        }
    }

    private Long getEntityTypeIdByName(String entityType) {
        String sql = "SELECT id FROM entity_types WHERE name = ? LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, entityType);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Сущность \"" + entityType + "\" не найдена.");
        }
    }
}
