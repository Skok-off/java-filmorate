package ru.yandex.practicum.filmorate.helper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.Map;

@Slf4j
public abstract class Helper {
    public static Long getNextId(Map<Long, ?> map) {
        long currentMaxId = map.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public static void handleError(String requestType, String errorMessage) {
        String message = requestType + " " + errorMessage;
        log.error(message);
        throw new ValidationException(message);
    }
}
