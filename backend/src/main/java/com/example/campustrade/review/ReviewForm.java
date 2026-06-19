package com.example.campustrade.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewForm {

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @NotNull(message = "请选择评分")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
    private Integer rating;

    @NotNull(message = "评价内容不能为空")
    @Size(min = 10, max = 200, message = "评价内容需在10-200字之间")
    private String content;
}
