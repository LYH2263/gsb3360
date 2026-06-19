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

    private Long itemId;

    private Long reviewerId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为 1 星")
    @Max(value = 5, message = "评分最高为 5 星")
    private Integer rating;

    @NotBlank(message = "评价内容不能为空")
    @Size(min = 10, max = 200, message = "评价内容长度需在 10 到 200 字之间")
    private String content;

    private LocalDateTime createdAt;

    private String reviewerUsername;
}
