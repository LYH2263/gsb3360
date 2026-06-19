package com.example.campustrade.mapper;

import com.example.campustrade.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(Review review);

    Review findByItemAndReviewer(@Param("itemId") Long itemId, @Param("reviewerId") Long reviewerId);

    List<Review> findByItemId(@Param("itemId") Long itemId);

    Double findAverageRatingByItemId(@Param("itemId") Long itemId);

    long countByItemId(@Param("itemId") Long itemId);
}
