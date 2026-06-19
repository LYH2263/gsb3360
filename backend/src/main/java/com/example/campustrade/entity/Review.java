package com.example.campustrade.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    private Long buyerId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 200, message = "评价内容长度必须在10-200字之间")
    private String content;

    private LocalDateTime createdAt;
}
