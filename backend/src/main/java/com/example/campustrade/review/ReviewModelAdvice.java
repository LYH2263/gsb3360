package com.example.campustrade.review;

import com.example.campustrade.entity.User;
import com.example.campustrade.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 在 items 相关页面（详情、我的订单）渲染前，把评价数据注入 Model。
 * 这样无需修改 ItemController/ItemService 现有方法签名，即可在原模板中显示评价信息。
 */
@ControllerAdvice
@RequiredArgsConstructor
public class ReviewModelAdvice {

    private static final Pattern ITEM_DETAIL_PATTERN = Pattern.compile("^/items/(\\d+)/?$");

    private final ReviewService reviewService;
    private final UserService userService;

    @ModelAttribute
    public void enrich(HttpServletRequest request, Model model, Authentication authentication) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return;
        }
        Long currentUserId = null;
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());
            if (user != null) {
                currentUserId = user.getId();
            }
        }

        Matcher m = ITEM_DETAIL_PATTERN.matcher(uri);
        if (m.matches()) {
            Long itemId = Long.parseLong(m.group(1));
            model.addAttribute("reviews", reviewService.listByItemId(itemId));
            model.addAttribute("reviewCount", reviewService.countByItemId(itemId));
            model.addAttribute("reviewAverage", reviewService.averageRating(itemId));
            model.addAttribute("canReview", currentUserId != null
                    && reviewService.canReviewByUserId(itemId, currentUserId));
        }

        if ("/items/orders".equals(uri) || "/items/orders/".equals(uri)) {
            model.addAttribute("orderCanReview", new OrderReviewLookup(reviewService, currentUserId, true));
            model.addAttribute("orderHasReview", new OrderReviewLookup(reviewService, currentUserId, false));
        }
    }

    /**
     * 在 Thymeleaf 中通过 ${orderCanReview[itemId]} / ${orderHasReview[itemId]} 查询评价状态。
     */
    public static class OrderReviewLookup extends HashMap<Long, Boolean> {
        private final transient ReviewService reviewService;
        private final Long userId;
        private final boolean canReviewMode;
        private final Map<Long, Boolean> cache = new HashMap<>();

        public OrderReviewLookup(ReviewService reviewService, Long userId, boolean canReviewMode) {
            this.reviewService = reviewService;
            this.userId = userId;
            this.canReviewMode = canReviewMode;
        }

        @Override
        public Boolean get(Object key) {
            if (!(key instanceof Long) && !(key instanceof Number)) {
                return false;
            }
            Long itemId = (key instanceof Long) ? (Long) key : ((Number) key).longValue();
            if (userId == null) {
                return false;
            }
            return cache.computeIfAbsent(itemId, id -> canReviewMode
                    ? reviewService.canReviewByUserId(id, userId)
                    : reviewService.hasReviewed(id, userId));
        }
    }
}
