package com.example.campustrade.controller;

import com.example.campustrade.entity.User;
import com.example.campustrade.entity.Item;
import com.example.campustrade.service.ItemService;
import com.example.campustrade.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ItemService itemService;

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

    private void fillAdminCommonModel(Model model, User operator) {
        model.addAttribute("currentUsername", operator.getUsername());
        model.addAttribute("currentUserId", operator.getId());
        model.addAttribute("isAdmin", "ADMIN".equalsIgnoreCase(operator.getRole()));
    }

    @GetMapping
    public String index() {
        return "redirect:/admin/items";
    }

    @GetMapping("/users")
    public String users(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model, Authentication authentication) {
        User operator = currentUser(authentication);
        fillAdminCommonModel(model, operator);
        int safeSize = Math.max(size, 1);
        long totalItems = userService.countAll();
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + safeSize - 1) / safeSize);
        int safePage = totalPages == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);
        model.addAttribute("users", totalItems == 0 ? java.util.List.of() : userService.listPage(safePage, safeSize));
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.adminDeleteUser(id, currentUser(authentication));
        return "redirect:/admin/users";
    }

    @GetMapping("/items")
    public String items(@RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model, Authentication authentication) {
        User operator = currentUser(authentication);
        fillAdminCommonModel(model, operator);
        int safeSize = Math.max(size, 1);
        long totalItems = itemService.countAll();
        int totalPages = totalItems == 0 ? 0 : (int) ((totalItems + safeSize - 1) / safeSize);
        int safePage = totalPages == 0 ? 1 : Math.min(Math.max(page, 1), totalPages);
        java.util.List<Item> items = totalItems == 0 ? java.util.List.of() : itemService.listPage(safePage, safeSize);
        Map<Long, String> userNameById = userService.listAll().stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
        model.addAttribute("items", items);
        model.addAttribute("userNameById", userNameById);
        model.addAttribute("page", safePage);
        model.addAttribute("size", safeSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        return "admin/items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable Long id, Authentication authentication) {
        itemService.adminDeleteById(id, currentUser(authentication));
        return "redirect:/admin/items";
    }
}
