package com.example.campustrade.controller;

import com.example.campustrade.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            if (isAdmin) {
                return "redirect:/admin/items";
            }
            return "redirect:/items";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form, BindingResult bindingResult, Model model) {
        if (form.getUsername() == null || form.getUsername().isBlank()) {
            bindingResult.rejectValue("username", "username.blank", "用户名不能为空");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "password.blank", "密码不能为空");
        } else if (form.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "password.length", "密码至少 6 位");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(form.getUsername(), form.getPassword(), form.getPhone());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "username.exists", e.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }

    @Data
    public static class RegisterForm {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        private String phone;
    }
}
