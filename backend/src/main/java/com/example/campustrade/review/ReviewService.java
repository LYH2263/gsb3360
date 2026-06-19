package com.example.campustrade.review;

import com.example.campustrade.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface ReviewService {

    void createReview(Long itemId, Integer rating, String content, User currentUser);

    List<ReviewDTO> getReviewsByItemId(Long itemId);

    BigDecimal getAverageRating(Long itemId);

    long getReviewCount(Long itemId);

    boolean hasReviewed(Long itemId, Long buyerId);

    Set<Long> getReviewedItemIds(Long buyerId);

    ReviewDTO getMyReviewForItem(Long itemId, Long buyerId);
}
