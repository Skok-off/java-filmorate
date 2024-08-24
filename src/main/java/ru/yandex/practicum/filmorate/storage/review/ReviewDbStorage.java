package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.ReviewValidator;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final ReviewMapper mapper;
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final ReviewValidator validate;

    public Review create(Review review) {
        validate.forCreate(review);
        String sql = "INSERT INTO reviews (content, user_id, film_id, is_positive) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setLong(2, review.getUserId());
            ps.setLong(3, review.getFilmId());
            ps.setBoolean(4, review.getIsPositive());
            return ps;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        review.setReviewId(id);
        review.setUseful(0);
        return review;
    }

    public Review update(Review review) {
        getReview(review.getReviewId());
        String sql = """
                UPDATE reviews
                SET content = COALESCE(?, content),
                    user_id = COALESCE(?, user_id),
                    film_id = COALESCE(?, film_id),
                    is_positive = COALESCE(?, is_positive)
                WHERE id = ?;
                """;
        jdbcTemplate.update(sql, review.getContent(), review.getUserId(), review.getFilmId(), review.getIsPositive(), review.getReviewId());
        return getReview(review.getReviewId());
    }

    public Review getReview(Long id) {
        String sql = """
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive, COALESCE(SUM(rl.rating), 0) AS rating_sum
                FROM reviews r
                LEFT JOIN review_likes rl ON rl.review_id = r.id
                WHERE r.id = ?
                GROUP BY r.id, r.content, r.user_id, r.film_id, r.is_positive;
                """;
        try {
            return jdbcTemplate.queryForObject(sql, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден.");
        }
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        filmDbStorage.getFilm(filmId);
        String sql = """
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive, COALESCE(SUM(rl.rating), 0) AS rating_sum
                FROM reviews r
                LEFT JOIN review_likes rl ON rl.review_id = r.id
                WHERE r.film_id = COALESCE(?, r.film_id)
                GROUP BY r.id, r.content, r.user_id, r.film_id, r.is_positive
                LIMIT ?;
                """;
        return jdbcTemplate.query(sql, mapper, filmId, count);
    }

    public void delete(Long id) {
        String sql = "DELETE reviews WHERE id = ?;";
        jdbcTemplate.update(sql, id);
    }

    //like и dislike отличаются лишь знаком + или -, одного метода будет достаточно
    public void setRating(Long userId, Long reviewId, Integer rating) {
        userDbStorage.getUser(userId);
        getReview(reviewId);
        String sql = """
                MERGE INTO review_likes (user_id, review_id, rating)
                KEY (user_id, review_id)
                VALUES (?, ?, ?);
                """;
        jdbcTemplate.update(sql, userId, reviewId, rating);
    }

    //тут тоже реитинг передаю, чтобы при удалении дизлайка не удалялся лайк и наоборот
    public void deleteRating(Long userId, Long reviewId, Integer rating) {
        userDbStorage.getUser(userId);
        getReview(reviewId);
        String sql = """
                DELETE review_likes
                WHERE user_id = ?
                AND review_id = ?
                AND rating = ?;
                """;
        jdbcTemplate.update(sql, userId, reviewId, rating);
    }
}
