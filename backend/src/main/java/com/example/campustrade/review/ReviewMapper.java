package com.example.campustrade.review;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewMapper {

    void insert(Review review);

    List<Review> findByItemId(@Param("itemId") Long itemId);

    Review findByItemAndBuyer(@Param("itemId") Long itemId, @Param("buyerId") Long buyerId);

    long countByItemId(@Param("itemId") Long itemId);

    Double averageRatingByItemId(@Param("itemId") Long itemId);
}
