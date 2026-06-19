package com.example.campustrade.controller;

import org.springframework.security.access.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/admin")) {
            if (uri.startsWith("/admin/items")) {
                return "redirect:/admin/items?forbidden";
            }
            return "redirect:/admin/users?forbidden";
        }
        if (uri != null && uri.startsWith("/reviews")) {
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/items/")) {
                return "redirect:" + referer.substring(referer.indexOf("/items/"));
            }
            return "redirect:/items?forbidden";
        }
        return "redirect:/items?forbidden";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/admin")) {
            if (uri.startsWith("/admin/items")) {
                return "redirect:/admin/items?error";
            }
            return "redirect:/admin/users?error";
        }
        if (uri != null && uri.startsWith("/reviews")) {
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/items/")) {
                return "redirect:" + referer.substring(referer.indexOf("/items/"));
            }
            return "redirect:/items?error";
        }
        return "redirect:/items?error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(HttpServletRequest request) {
        return "redirect:/login";
    }
}
