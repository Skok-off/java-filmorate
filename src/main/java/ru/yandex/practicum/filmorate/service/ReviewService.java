package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;

    public Review update(Review review) {
        return reviewDbStorage.update(review);
    }

    public Review create(Review review) {
        return reviewDbStorage.create(review);
    }

    public void delete(Long id) {
        reviewDbStorage.delete(id);
    }

    public Review getReview(Long id) {
        return reviewDbStorage.getReview(id);
    }

    public List<Review> getReviews(Long filmId, Integer count) {
        return reviewDbStorage.getReviews(filmId, count);
    }

    public void updateLike(Long id, Long userId) {
        reviewDbStorage.setRating(userId, id, 1);
    }

    public void updateDislike(Long id, Long userId) {
        reviewDbStorage.setRating(userId, id, -1);
    }

    public void deleteLike(Long id, Long userId) {
        reviewDbStorage.deleteRating(userId, id, 1);
    }

    public void deleteDislike(Long id, Long userId) {
        reviewDbStorage.deleteRating(userId, id, -1);
    }
}
