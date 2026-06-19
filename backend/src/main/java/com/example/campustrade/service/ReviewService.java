package com.example.campustrade.service;

import com.example.campustrade.dto.ReviewDTO;
import com.example.campustrade.entity.Review;
import com.example.campustrade.entity.User;

import java.util.List;

public interface ReviewService {

    void createReview(Review review, User currentUser);

    List<ReviewDTO> getReviewsByItemId(Long itemId);

    Double getAverageRating(Long itemId);

    String getFormattedAverageRating(Long itemId);

    int getRoundedAverageRating(Long itemId);

    long getReviewCount(Long itemId);

    boolean hasUserReviewed(Long itemId, Long userId);

    List<Long> getReviewedItemIdsByBuyerId(Long buyerId);
}
