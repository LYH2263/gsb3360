package com.example.campustrade.review;

import com.example.campustrade.entity.User;

import java.util.List;

public interface ReviewService {

    List<Review> listByItemId(Long itemId);

    long countByItemId(Long itemId);

    /**
     * 平均分（保留 1 位小数），无评价返回 null。
     */
    Double averageRating(Long itemId);

    /**
     * 当前用户是否已对该商品评价过。
     */
    boolean hasReviewed(Long itemId, Long buyerId);

    /**
     * 当前用户是否有资格评价该商品。
     * 必须：登录、非管理员、是该商品的 buyer、商品状态为 SOLD、未评价过。
     */
    boolean canReview(Long itemId, User currentUser);

    /**
     * 模板渲染时可用的便捷方法，按用户 id 判断是否可评价。
     */
    boolean canReviewByUserId(Long itemId, Long userId);

    /**
     * 提交评价，包含完整业务规则校验。
     */
    void submit(Long itemId, ReviewForm form, User currentUser);
}
