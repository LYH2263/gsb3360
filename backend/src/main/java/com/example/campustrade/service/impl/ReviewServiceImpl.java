package com.example.campustrade.service.impl;

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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewMapper reviewMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public void submitReview(Long itemId, Review review, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new AccessDeniedException("管理员不可提交评价");
        }

        Item item = itemMapper.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalArgumentException("仅已成交商品可评价");
        }

        if (item.getSellerId() != null && item.getSellerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("卖家不可评价自己发布的商品");
        }

        if (item.getBuyerId() == null || !item.getBuyerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("仅购买者可评价该商品");
        }

        Review existing = reviewMapper.findByItemAndReviewer(itemId, currentUser.getId());
        if (existing != null) {
            throw new IllegalArgumentException("您已对该商品提交过评价，不可重复提交");
        }

        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在 1 到 5 星之间");
        }

        String content = review.getContent();
        if (content == null || content.length() < 10 || content.length() > 200) {
            throw new IllegalArgumentException("评价内容长度需在 10 到 200 字之间");
        }

        review.setItemId(itemId);
        review.setReviewerId(currentUser.getId());
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        log.info("Review submitted itemId={} reviewerId={} rating={}", itemId, currentUser.getId(), review.getRating());
    }

    @Override
    public boolean hasReviewed(Long itemId, Long userId) {
        return reviewMapper.findByItemAndReviewer(itemId, userId) != null;
    }

    @Override
    public List<Review> listByItemId(Long itemId) {
        return reviewMapper.findByItemId(itemId);
    }

    @Override
    public Double getAverageRating(Long itemId) {
        return reviewMapper.findAverageRatingByItemId(itemId);
    }

    @Override
    public long countByItemId(Long itemId) {
        return reviewMapper.countByItemId(itemId);
    }
}
