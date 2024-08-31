package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class ReviewValidator {

    private final UserDbStorage userDbStorage;
    private final FilmDbStorage filmDbStorage;

    public void forCreate(Review review) {
        if (Objects.isNull(review.getUserId())) {
            throw new ValidationException("некорректный id пользователя.");
        }
        if (Objects.isNull(review.getFilmId())) {
            throw new ValidationException("некорректный id фильма.");
        }
        userDbStorage.getUser(review.getUserId());
        filmDbStorage.getFilm(review.getFilmId());
        if (Objects.isNull(review.getIsPositive())) {
            throw new ValidationException("Не указано положительный или отрицательный отзыв.");
        }
        if (Objects.isNull(review.getContent()) || review.getContent().isBlank()) {
            throw new ValidationException("Не указан текст отзыва");
        }
    }

}
