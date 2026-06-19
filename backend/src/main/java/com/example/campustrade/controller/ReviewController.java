package com.example.campustrade.controller;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.Review;
import com.example.campustrade.entity.User;
import com.example.campustrade.service.ItemService;
import com.example.campustrade.service.ReviewService;
import com.example.campustrade.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final ItemService itemService;
    private final UserService userService;

    @ModelAttribute
    public void addCommonModel(Model model, Authentication authentication) {
        if (authentication == null) {
            return;
        }
        User user = userService.findByUsername(authentication.getName());
        if (user != null) {
            model.addAttribute("currentUsername", user.getUsername());
            model.addAttribute("currentUserId", user.getId());
            model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(user.getRole()));
        }
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("未登录");
        }
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            throw new IllegalStateException("用户不存在");
        }
        return user;
    }

    @GetMapping("/new/{itemId}")
    public String reviewForm(@PathVariable Long itemId, Model model, Authentication authentication) {
        User user = currentUser(authentication);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/items?adminReadonly";
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            log.warn("Item not found id={}", itemId);
            return "redirect:/items";
        }

        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            return "redirect:/items?error";
        }

        if (item.getSellerId() != null && item.getSellerId().equals(user.getId())) {
            return "redirect:/items?forbidden";
        }

        if (item.getBuyerId() == null || !item.getBuyerId().equals(user.getId())) {
            return "redirect:/items?forbidden";
        }

        if (reviewService.hasReviewed(itemId, user.getId())) {
            return "redirect:/items/" + itemId + "?reviewed";
        }

        model.addAttribute("item", item);
        if (!model.containsAttribute("review")) {
            model.addAttribute("review", new Review());
        }
        return "reviews/form";
    }

    @PostMapping("/{itemId}")
    public String submitReview(@PathVariable Long itemId,
                               @Valid @ModelAttribute("review") Review review,
                               BindingResult bindingResult,
                               Model model,
                               Authentication authentication) {
        User user = currentUser(authentication);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return "redirect:/items?adminReadonly";
        }

        Item item = itemService.getById(itemId);
        if (item == null) {
            return "redirect:/items";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "reviews/form";
        }

        reviewService.submitReview(itemId, review, user);
        return "redirect:/items/" + itemId;
    }
}
