package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EventDbStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserDbStorage userDbStorage;
    private final EventMapper mapper;

    public List<Event> getUserEvents(Long userId) {
        userDbStorage.getUser(userId);
        String sql = """
                SELECT e.datetime, e.user_id, et.name AS et_name, o.name AS o_name, e.id, e.entity_id
                FROM events e
                JOIN operations o ON e.operation_id = o.id
                JOIN event_types et ON e.event_type_id = et.id
                WHERE e.user_id = :id
                ORDER BY e.datetime;
                """;
        return namedParameterJdbcTemplate.query(sql, Map.of("id", userId), mapper);
    }

    public void add(Long userId, Long entityId, String entityType, String operation, String eventType) {
        Long operationId = getOperationIdByName(operation);
        Long eventTypeId = getEventTypeIdByName(eventType);
        Long entityTypeId = getEntityTypeIdByName(entityType);
        String sql = "INSERT INTO events (user_id, entity_id, entity_type_id, operation_id, event_type_id)" +
                "VALUES(:userId, :entityId, :entityTypeId, :operationId, :eventTypeId)";
        namedParameterJdbcTemplate.update(sql, Map.of("userId", userId,
                "entityId", entityId,
                "entityTypeId", entityTypeId,
                "operationId", operationId,
                "eventTypeId", eventTypeId));
    }

    private Long getOperationIdByName(String operation) {
        String sql = "SELECT id FROM operations WHERE name = :operation LIMIT 1";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("operation", operation), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Операция \"" + operation + "\" не найдена.");
        }
    }

    private Long getEventTypeIdByName(String eventType) {
        String sql = "SELECT id FROM event_types WHERE name = :eventType LIMIT 1";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("eventType", eventType), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Тип события \"" + eventType + "\" не найден.");
        }
    }

    private Long getEntityTypeIdByName(String entityType) {
        String sql = "SELECT id FROM entity_types WHERE name = :entityType LIMIT 1";
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("entityType", entityType), Long.class);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Сущность \"" + entityType + "\" не найдена.");
        }
    }
}
