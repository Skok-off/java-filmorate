package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Review {
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
