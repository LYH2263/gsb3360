package com.example.campustrade.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return "redirect:/admin/items";
        }
        return "redirect:/items";
    }
}
