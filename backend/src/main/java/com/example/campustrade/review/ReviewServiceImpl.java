package com.example.campustrade.review;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewMapper reviewMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public void createReview(Long itemId, Integer rating, String content, User currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("未登录");
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("管理员不可提交评价");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("评分必须在1-5星之间");
        }
        if (content == null || content.length() < 10 || content.length() > 200) {
            throw new IllegalArgumentException("评价内容需在10-200字之间");
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
            throw new AccessDeniedException("仅商品实际买家可提交评价");
        }

        ReviewDTO existing = reviewMapper.findByItemIdAndBuyerId(itemId, currentUser.getId());
        if (existing != null) {
            throw new IllegalArgumentException("您已对该商品提交过评价，不可重复评价");
        }

        reviewMapper.insert(itemId, currentUser.getId(), rating, content, LocalDateTime.now());
        log.info("Review created itemId={} buyerId={} rating={}", itemId, currentUser.getId(), rating);
    }

    @Override
    public List<ReviewDTO> getReviewsByItemId(Long itemId) {
        return reviewMapper.findByItemId(itemId);
    }

    @Override
    public BigDecimal getAverageRating(Long itemId) {
        Double avg = reviewMapper.findAverageRatingByItemId(itemId);
        if (avg == null) {
            return null;
        }
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public long getReviewCount(Long itemId) {
        return reviewMapper.countByItemId(itemId);
    }

    @Override
    public boolean hasReviewed(Long itemId, Long buyerId) {
        if (itemId == null || buyerId == null) {
            return false;
        }
        return reviewMapper.findByItemIdAndBuyerId(itemId, buyerId) != null;
    }

    @Override
    public Set<Long> getReviewedItemIds(Long buyerId) {
        if (buyerId == null) {
            return new HashSet<>();
        }
        return new HashSet<>(reviewMapper.findReviewedItemIdsByBuyerId(buyerId));
    }

    @Override
    public ReviewDTO getMyReviewForItem(Long itemId, Long buyerId) {
        if (itemId == null || buyerId == null) {
            return null;
        }
        return reviewMapper.findByItemIdAndBuyerId(itemId, buyerId);
    }
}
