package com.example.campustrade.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewForm {

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分必须在 1-5 之间")
    @Max(value = 5, message = "评分必须在 1-5 之间")
    private Integer rating;

    @NotNull(message = "请填写评价内容")
    @Size(min = 10, max = 200, message = "评价内容长度需在 10-200 字之间")
    private String content;
}
