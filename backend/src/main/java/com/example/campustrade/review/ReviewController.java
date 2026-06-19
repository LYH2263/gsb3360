package com.example.campustrade.review;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import com.example.campustrade.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
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
    private final ItemMapper itemMapper;
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
            throw new AccessDeniedException("未登录");
        }
        User user = userService.findByUsername(authentication.getName());
        if (user == null) {
            throw new AccessDeniedException("用户不存在");
        }
        return user;
    }

    @GetMapping("/items/{itemId}/new")
    public String newForm(@PathVariable Long itemId, Model model, Authentication authentication) {
        User user = currentUser(authentication);
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            return "redirect:/items/orders?error";
        }
        if (!reviewService.canReview(itemId, user)) {
            return "redirect:/items/" + itemId;
        }
        model.addAttribute("item", item);
        if (!model.containsAttribute("reviewForm")) {
            model.addAttribute("reviewForm", new ReviewForm());
        }
        return "review/form";
    }

    @PostMapping("/items/{itemId}")
    public String submit(@PathVariable Long itemId,
                         @Valid @ModelAttribute("reviewForm") ReviewForm reviewForm,
                         BindingResult bindingResult,
                         Model model,
                         Authentication authentication) {
        User user = currentUser(authentication);
        Item item = itemMapper.findById(itemId);
        if (item == null) {
            return "redirect:/items/orders?error";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "review/form";
        }
        reviewService.submit(itemId, reviewForm, user);
        log.info("Review accepted itemId={} buyerId={}", itemId, user.getId());
        return "redirect:/items/" + itemId + "?reviewed";
    }
}
