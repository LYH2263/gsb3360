package com.example.campustrade.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Item {
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    private Long sellerId;

    private Long buyerId;

    private String status;

    private Long categoryId;

    private LocalDateTime createdAt;

    private LocalDateTime tradedAt;
}
