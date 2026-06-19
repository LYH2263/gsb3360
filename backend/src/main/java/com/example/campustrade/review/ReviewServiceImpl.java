package com.example.campustrade.review;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import com.example.campustrade.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service("reviewService")
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewMapper reviewMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public List<Review> listByItemId(Long itemId) {
        return reviewMapper.findByItemId(itemId);
    }

    @Override
    public long countByItemId(Long itemId) {
        return reviewMapper.countByItemId(itemId);
    }

    @Override
    public Double averageRating(Long itemId) {
        Double avg = reviewMapper.averageRatingByItemId(itemId);
        if (avg == null) {
            return null;
        }
        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public boolean hasReviewed(Long itemId, Long buyerId) {
        if (itemId == null || buyerId == null) {
            return false;
        }
        return reviewMapper.findByItemAndBuyer(itemId, buyerId) != null;
    }

    @Override
    public boolean canReview(Long itemId, User currentUser) {
        if (currentUser == null || itemId == null) {
            return false;
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return false;
        }
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            return false;
        }
        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            return false;
        }
        if (item.getBuyerId() == null || !item.getBuyerId().equals(currentUser.getId())) {
            return false;
        }
        return !hasReviewed(itemId, currentUser.getId());
    }

    @Override
    public boolean canReviewByUserId(Long itemId, Long userId) {
        if (itemId == null || userId == null) {
            return false;
        }
        User user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        return canReview(itemId, user);
    }

    @Override
    @Transactional
    public void submit(Long itemId, ReviewForm form, User currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("未登录");
        }
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new AccessDeniedException("管理员不可提交评价");
        }
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (item.getSellerId() != null && item.getSellerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("卖家不可对自己发布的商品评价");
        }
        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalArgumentException("仅已成交商品可评价");
        }
        if (item.getBuyerId() == null || !item.getBuyerId().equals(currentUser.getId())) {
            throw new AccessDeniedException("只有买家可对该商品评价");
        }
        if (form == null || form.getRating() == null || form.getContent() == null) {
            throw new IllegalArgumentException("评价信息不完整");
        }
        if (form.getRating() < 1 || form.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在 1-5 之间");
        }
        String content = form.getContent().trim();
        if (content.length() < 10 || content.length() > 200) {
            throw new IllegalArgumentException("评价内容长度需在 10-200 字之间");
        }
        if (hasReviewed(itemId, currentUser.getId())) {
            throw new IllegalArgumentException("你已评价过该商品");
        }
        Review review = new Review();
        review.setItemId(itemId);
        review.setBuyerId(currentUser.getId());
        review.setRating(form.getRating());
        review.setContent(content);
        review.setCreatedAt(LocalDateTime.now());
        reviewMapper.insert(review);
        log.info("Review submitted itemId={} buyerId={} rating={}", itemId, currentUser.getId(), form.getRating());
    }
}
