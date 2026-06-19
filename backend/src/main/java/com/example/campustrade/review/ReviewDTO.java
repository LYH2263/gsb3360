package com.example.campustrade.review;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long itemId;
    private Long buyerId;
    private String buyerUsername;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
}
