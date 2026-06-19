package com.example.campustrade.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long itemId;
    private Long buyerId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    private String buyerUsername;
}
