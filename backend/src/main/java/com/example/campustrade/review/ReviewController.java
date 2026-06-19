package com.example.campustrade.review;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    private final ReviewService reviewService;
    private final UserService userService;
    private final ItemMapper itemMapper;

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

    @GetMapping("/new")
    public String reviewForm(@RequestParam("itemId") Long itemId,
                             Model model,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = currentUser(authentication);
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "管理员不可提交评价");
            return "redirect:/items/" + itemId;
        }

        Item item = itemMapper.findById(itemId);
        if (item == null) {
            redirectAttributes.addFlashAttribute("error", "商品不存在");
            return "redirect:/items";
        }

        if (!"SOLD".equalsIgnoreCase(item.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "仅已成交商品可评价");
            return "redirect:/items/" + itemId;
        }

        if (item.getSellerId() != null && item.getSellerId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "卖家不可评价自己发布的商品");
            return "redirect:/items/" + itemId;
        }

        if (item.getBuyerId() == null || !item.getBuyerId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "仅商品实际买家可提交评价");
            return "redirect:/items/" + itemId;
        }

        if (reviewService.hasReviewed(itemId, user.getId())) {
            redirectAttributes.addFlashAttribute("error", "您已对该商品提交过评价");
            return "redirect:/items/" + itemId;
        }

        ReviewForm form = new ReviewForm();
        form.setItemId(itemId);
        model.addAttribute("reviewForm", form);
        model.addAttribute("item", item);
        return "reviews/form";
    }

    @PostMapping
    public String submitReview(@Valid @ModelAttribute("reviewForm") ReviewForm reviewForm,
                               BindingResult bindingResult,
                               Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = currentUser(authentication);

        Item item = itemMapper.findById(reviewForm.getItemId());
        if (item != null) {
            model.addAttribute("item", item);
        }

        if (bindingResult.hasErrors()) {
            return "reviews/form";
        }

        try {
            reviewService.createReview(reviewForm.getItemId(), reviewForm.getRating(),
                    reviewForm.getContent(), user);
            redirectAttributes.addFlashAttribute("success", "评价提交成功");
            return "redirect:/items/" + reviewForm.getItemId();
        } catch (IllegalArgumentException | IllegalStateException | org.springframework.security.access.AccessDeniedException e) {
            model.addAttribute("error", e.getMessage());
            return "reviews/form";
        }
    }
}
