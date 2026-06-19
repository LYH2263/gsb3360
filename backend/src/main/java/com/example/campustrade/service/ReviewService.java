package com.example.campustrade.service;

import com.example.campustrade.entity.Review;
import com.example.campustrade.entity.User;

import java.util.List;

public interface ReviewService {

    void submitReview(Long itemId, Review review, User currentUser);

    boolean hasReviewed(Long itemId, Long userId);

    List<Review> listByItemId(Long itemId);

    Double getAverageRating(Long itemId);

    long countByItemId(Long itemId);
}
