package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.ReviewValidator;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ReviewDbStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ReviewMapper mapper;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final ReviewValidator validate;

    public Review create(Review review) {
        validate.forCreate(review);
        String sql = """
            INSERT INTO reviews (content, user_id, film_id, is_positive)
            VALUES (:content, :user_id, :film_id, :is_positive)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("content", review.getContent())
            .addValue("user_id", review.getUserId())
            .addValue("film_id", review.getFilmId())
            .addValue("is_positive", review.getIsPositive());
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[] {"id"});
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(id);
        review.setUseful(0);

        return review;
    }

    public Review update(Review review) {
        getReview(review.getReviewId());
        String sql = """
            UPDATE reviews
            SET content = COALESCE(:content, content),
                is_positive = COALESCE(:is_positive, is_positive)
            WHERE id = :id;
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("content", review.getContent())
            .addValue("is_positive", review.getIsPositive())
            .addValue("id", review.getIsPositive());
        namedParameterJdbcTemplate.update(sql, params);

        return getReview(review.getReviewId());
    }

    public Review getReview(Long id) {
        String sql = """
            SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive, COALESCE(SUM(rl.rating), 0) AS rating_sum
            FROM reviews r
            LEFT JOIN review_likes rl ON rl.review_id = r.id
            WHERE r.id = :id
            GROUP BY r.id, r.content, r.user_id, r.film_id, r.is_positive;
            """;
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), mapper);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден.");
        }
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        if (Objects.nonNull(filmId)) {
            filmDbStorage.getFilm(filmId);
        }
        String sql = """
            SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive, COALESCE(SUM(rl.rating), 0) AS rating_sum
            FROM reviews r
            LEFT JOIN review_likes rl ON rl.review_id = r.id
            WHERE r.film_id = COALESCE(:filmId, r.film_id)
            GROUP BY r.id, r.content, r.user_id, r.film_id, r.is_positive
            ORDER BY COALESCE(SUM(rl.rating), 0) DESC
            LIMIT :count;
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("filmId", filmId)
            .addValue("count", count);

        return namedParameterJdbcTemplate.query(sql, params, mapper);
    }

    public void delete(Long id) {
        String sql = "DELETE FROM reviews WHERE id = :id;";

        namedParameterJdbcTemplate.update(sql, Map.of("id", id));
    }

    //Like и dislike отличаются лишь знаком "+" или "-". Одного метода будет достаточно
    public void setRating(Long userId, Long reviewId, Integer rating) {
        userDbStorage.getUser(userId);
        getReview(reviewId);
        String sql = """
            MERGE INTO review_likes (user_id, review_id, rating)
            KEY (user_id, review_id)
            VALUES (:userId, :reviewId, :rating);
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("reviewId", reviewId)
            .addValue("rating", rating);

        namedParameterJdbcTemplate.update(sql, params);
    }

    //тут тоже реитинг передаю, чтобы при удалении дизлайка не удалялся лайк и наоборот
    public void deleteRating(Long userId, Long reviewId, Integer rating) {
        userDbStorage.getUser(userId);
        getReview(reviewId);
        String sql = """
            DELETE FROM review_likes
            WHERE user_id = :userId
            AND review_id = :reviewId
            AND rating = :rating;
            """;
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("reviewId", reviewId)
            .addValue("rating", rating);

        namedParameterJdbcTemplate.update(sql, params);
    }

}