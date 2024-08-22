package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class EventDbStorage {
    @Autowired
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private final UserDbStorage userDbStorage;
    @Autowired
    private final EventMapper eventMapper;

    public Collection<Event> getUserEvents(Long userId) {
        User user = userDbStorage.getUser(userId);
        if (Objects.isNull(user)) throw new NotFoundException("Пользователь " + userId + " не найден");
        String sql = "SELECT e.datetime, e.user_id, et.name AS et_name, o.name AS o_name, e.id, e.entity_id " +
                "FROM events e " +
                "JOIN operations o ON e.operation_id = o.id " +
                "JOIN event_types et ON e.event_type_id = et.id " +
                "WHERE e.user_id = ?" +
                "ORDER BY e.datetime DESC";
        return jdbcTemplate.query(sql, eventMapper::mapRow, userId);
    }

    public void add(Long userId, Long entityId, String entityType, String operation, String eventType) {
        Long operationId = getOperationIdByName(operation);
        Long eventTypeId = getEventTypeIdByName(eventType);
        Long entityTypeId = getEntityTypeIdByName(entityType);
        String sql = "INSERT INTO event (user_id, entity_id, entity_type_id, operation_id, event_type_id)" +
                "VALUES(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, entityId, entityTypeId, operationId, eventTypeId);
    }

    private Long getOperationIdByName(String operation) {
        String sql = "SELECT id FROM operations WHERE name = ? LIMIT 1";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, operation);
        if (Objects.isNull(id)) throw new NotFoundException("Операция \"" + operation + "\" не найдена.");
        return id;
    }

    private Long getEventTypeIdByName(String eventType) {
        String sql = "SELECT id FROM event_types WHERE name = ? LIMIT 1";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, eventType);
        if (Objects.isNull(id)) throw new NotFoundException("Тип события \"" + eventType + "\" не найдена.");
        return id;
    }

    private Long getEntityTypeIdByName(String entityType) {
        String sql = "SELECT id FROM entity_types WHERE name = ? LIMIT 1";
        Long id = jdbcTemplate.queryForObject(sql, Long.class, entityType);
        if (Objects.isNull(id)) throw new NotFoundException("Сущности \"" + entityType + "\" не найдена.");
        return id;
    }
}
