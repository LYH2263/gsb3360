package com.example.campustrade.service.impl;

import com.example.campustrade.dto.ReviewDTO;
import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.Review;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import com.example.campustrade.mapper.ReviewMapper;
import com.example.campustrade.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewMapper reviewMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public void createReview(Review review, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("管理员账号不可提交评价");
        }

        Item item = itemMapper.findById(review.getItemId());
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalArgumentException("只能对已成交(SOLD)的商品提交评价");
        }

        if (item.getBuyerId() == null || !item.getBuyerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("只有该商品的实际买家才能提交评价");
        }

        if (item.getSellerId() != null && item.getSellerId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("不能评价自己发布的商品");
        }

        Review existing = reviewMapper.findByItemIdAndBuyerId(review.getItemId(), currentUser.getId());
        if (existing != null) {
            throw new IllegalArgumentException("您已对该商品提交过评价，不能重复评价");
        }

        review.setBuyerId(currentUser.getId());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        log.info("Review created itemId={} buyerId={} rating={}", review.getItemId(), review.getBuyerId(), review.getRating());
    }

    @Override
    public List<ReviewDTO> getReviewsByItemId(Long itemId) {
        List<ReviewDTO> reviews = reviewMapper.findByItemId(itemId);
        return reviews != null ? reviews : Collections.emptyList();
    }

    @Override
    public Double getAverageRating(Long itemId) {
        return reviewMapper.findAverageRatingByItemId(itemId);
    }

    @Override
    public String getFormattedAverageRating(Long itemId) {
        Double avg = getAverageRating(itemId);
        if (avg == null) {
            return "0.0";
        }
        BigDecimal bd = BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }

    @Override
    public int getRoundedAverageRating(Long itemId) {
        Double avg = getAverageRating(itemId);
        if (avg == null) {
            return 0;
        }
        return (int) Math.round(avg);
    }

    @Override
    public long getReviewCount(Long itemId) {
        return reviewMapper.countByItemId(itemId);
    }

    @Override
    public boolean hasUserReviewed(Long itemId, Long userId) {
        return reviewMapper.findByItemIdAndBuyerId(itemId, userId) != null;
    }

    @Override
    public List<Long> getReviewedItemIdsByBuyerId(Long buyerId) {
        List<Review> reviews = reviewMapper.findByBuyerId(buyerId);
        if (reviews == null) {
            return Collections.emptyList();
        }
        return reviews.stream().map(Review::getItemId).collect(Collectors.toList());
    }
}
