package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder(toBuilder = true)
public class Event {
    private Timestamp timestamp;
    private Long userId;
    private String eventType;
    private String operation;
    private Long eventId; //primary key
    private Long entityId;
}
