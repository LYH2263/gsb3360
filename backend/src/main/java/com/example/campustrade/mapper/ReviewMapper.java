package com.example.campustrade.mapper;

import com.example.campustrade.dto.ReviewDTO;
import com.example.campustrade.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(Review review);

    Review findByItemIdAndBuyerId(@Param("itemId") Long itemId, @Param("buyerId") Long buyerId);

    List<ReviewDTO> findByItemId(@Param("itemId") Long itemId);

    Double findAverageRatingByItemId(@Param("itemId") Long itemId);

    long countByItemId(@Param("itemId") Long itemId);

    List<Review> findByBuyerId(@Param("buyerId") Long buyerId);
}
