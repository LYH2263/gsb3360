package com.example.campustrade.controller;

import com.example.campustrade.entity.Item;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final UserService userService;
    private final ReviewService reviewService;

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

    private boolean isAdmin(User user) {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }

    private String resolveReturnUrl(String from, Integer page, Integer size) {
        int safePage = page == null ? 1 : Math.max(page, 1);
        int safeSize = size == null ? 10 : Math.max(size, 1);
        if ("mine".equalsIgnoreCase(from)) {
            return UriComponentsBuilder.fromPath("/items/mine")
                    .queryParam("page", safePage)
                    .queryParam("size", safeSize)
                    .build()
                    .toUriString();
        }
        return UriComponentsBuilder.fromPath("/items")
                .queryParam("page", safePage)
                .queryParam("size", safeSize)
                .build()
                .toUriString();
    }

    private Map<Long, String> userNameById() {
        return userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
    }

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model,
                       Authentication authentication) {
        int safeSize = Math.max(size, 1);
        long totalItems = itemService.countActive();
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + safeSize - 1) / safeSize);
        int safePage = totalPages == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);

        model.addAttribute("items", totalItems == 0 ? java.util.List.of() : itemService.listActivePage(safePage, safeSize));
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        return "items/list";
    }

    @GetMapping("/mine")
    public String myItems(@RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "size", defaultValue = "10") int size,
                          Model model, Authentication authentication) {
        User user = currentUser(authentication);
        int safeSize = Math.max(size, 1);
        long totalItems = itemService.countBySellerId(user.getId());
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + safeSize - 1) / safeSize);
        int safePage = totalPages == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);
        model.addAttribute("items", totalItems == 0 ? java.util.List.of() : itemService.listBySellerPage(user.getId(), safePage, safeSize));
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("userNameById", userNameById());
        return "items/mine";
    }

    @GetMapping("/orders")
    public String myOrders(@RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "size", defaultValue = "10") int size,
                           Model model, Authentication authentication) {
        User user = currentUser(authentication);
        int safeSize = Math.max(size, 1);
        long totalItems = itemService.countByBuyerId(user.getId());
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + safeSize - 1) / safeSize);
        int safePage = totalPages == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);
        java.util.List<Item> orderItems = totalItems == 0 ? java.util.List.of() : itemService.listByBuyerPage(user.getId(), safePage, safeSize);
        model.addAttribute("items", orderItems);
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("userNameById", userNameById());
        java.util.Map<Long, Boolean> reviewedMap = new java.util.HashMap<>();
        for (Item it : orderItems) {
            reviewedMap.put(it.getId(), reviewService.hasReviewed(it.getId(), user.getId()));
        }
        model.addAttribute("reviewedMap", reviewedMap);
        return "items/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Item item = itemService.getById(id);
        if (item == null) {
            log.warn("Item not found id={}", id);
            return "redirect:/items";
        }
        model.addAttribute("item", item);
        model.addAttribute("reviews", reviewService.listByItemId(id));
        Double avg = reviewService.getAverageRating(id);
        model.addAttribute("averageRating", avg == null ? null : String.format("%.1f", avg));
        model.addAttribute("averageRatingRounded", avg == null ? 0 : (int) Math.round(avg));
        model.addAttribute("reviewCount", reviewService.countByItemId(id));
        if (authentication != null) {
            User user = userService.findByUsername(authentication.getName());
            if (user != null) {
                boolean canReview = !"ADMIN".equalsIgnoreCase(user.getRole())
                        && "SOLD".equalsIgnoreCase(item.getStatus())
                        && item.getBuyerId() != null
                        && item.getBuyerId().equals(user.getId())
                        && item.getSellerId() != null
                        && !item.getSellerId().equals(user.getId())
                        && !reviewService.hasReviewed(id, user.getId());
                model.addAttribute("canReview", canReview);
            }
        }
        return "items/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model, Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        model.addAttribute("item", new Item());
        return "items/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(value = "from", required = false) String from,
                           @RequestParam(value = "page", required = false) Integer page,
                           @RequestParam(value = "size", required = false) Integer size,
                           Model model, Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        Item item = itemService.getById(id);
        if (item == null) {
            log.warn("Item not found id={}", id);
            return "redirect:/items";
        }
        if (item.getSellerId() == null || !item.getSellerId().equals(user.getId())) {
            return "redirect:/items?forbidden";
        }
        model.addAttribute("item", item);
        model.addAttribute("from", from);
        model.addAttribute("returnPage", page == null ? 1 : Math.max(page, 1));
        model.addAttribute("returnSize", size == null ? 10 : Math.max(size, 1));
        return "items/form";
    }

    @PostMapping
    public String create(@Valid Item item, BindingResult bindingResult, Model model, Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            return "items/form";
        }
        itemService.create(item, user);
        return "redirect:/items";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam(value = "from", required = false) String from,
                         @RequestParam(value = "page", required = false) Integer page,
                         @RequestParam(value = "size", required = false) Integer size,
                         @Valid Item item, BindingResult bindingResult, Model model, Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("item", item);
            model.addAttribute("from", from);
            model.addAttribute("returnPage", page == null ? 1 : Math.max(page, 1));
            model.addAttribute("returnSize", size == null ? 10 : Math.max(size, 1));
            return "items/form";
        }
        item.setId(id);
        itemService.update(item, user);
        return "redirect:" + resolveReturnUrl(from, page, size);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(value = "from", required = false) String from,
                         @RequestParam(value = "page", required = false) Integer page,
                         @RequestParam(value = "size", required = false) Integer size,
                         Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        itemService.deleteById(id, user);
        return "redirect:" + resolveReturnUrl(from, page, size);
    }

    @PostMapping("/{id}/trade")
    public String trade(@PathVariable Long id, Authentication authentication) {
        User user = currentUser(authentication);
        if (isAdmin(user)) {
            return "redirect:/items?adminReadonly";
        }
        itemService.completeTrade(id, user);
        return "redirect:/items/" + id;
    }
}
