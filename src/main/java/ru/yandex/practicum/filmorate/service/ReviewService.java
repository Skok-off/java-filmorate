package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.EventDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;
    private final EventDbStorage eventDbStorage;

    public Review update(Review review) {
        Review updatedReview = reviewDbStorage.update(review);
        eventDbStorage.add(updatedReview.getUserId(), updatedReview.getReviewId(), "reviews", "UPDATE", "REVIEW");
        return updatedReview;
    }

    public Review create(Review review) {
        Review createdReview = reviewDbStorage.create(review);
        eventDbStorage.add(createdReview.getUserId(), createdReview.getReviewId(), "reviews", "ADD", "REVIEW");
        return createdReview;
    }

    public void delete(Long id) {
        Review deletedReview = getReview(id);
        reviewDbStorage.delete(id);
        eventDbStorage.add(deletedReview.getUserId(), deletedReview.getReviewId(), "reviews", "REMOVE", "REVIEW");
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
