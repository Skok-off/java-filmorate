package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
@Builder(toBuilder = true)
public class Genre {
    Long id;
    String name;
}
