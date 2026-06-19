package com.example.campustrade.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String role;
    private LocalDateTime createdAt;
}

