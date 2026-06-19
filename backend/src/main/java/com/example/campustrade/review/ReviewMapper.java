package com.example.campustrade.review;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(@Param("itemId") Long itemId,
                @Param("buyerId") Long buyerId,
                @Param("rating") Integer rating,
                @Param("content") String content,
                @Param("createdAt") LocalDateTime createdAt);

    List<ReviewDTO> findByItemId(@Param("itemId") Long itemId);

    ReviewDTO findByItemIdAndBuyerId(@Param("itemId") Long itemId, @Param("buyerId") Long buyerId);

    Double findAverageRatingByItemId(@Param("itemId") Long itemId);

    long countByItemId(@Param("itemId") Long itemId);

    List<Long> findReviewedItemIdsByBuyerId(@Param("buyerId") Long buyerId);
}
