package com.example.campustrade.mapper;

import com.example.campustrade.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    List<Item> findAll();

    List<Item> findPage(@Param("offset") int offset, @Param("limit") int limit);

    List<Item> findActivePage(@Param("offset") int offset, @Param("limit") int limit);

    List<Item> findBySellerId(@Param("sellerId") Long sellerId);

    List<Item> findBySellerPage(@Param("sellerId") Long sellerId, @Param("offset") int offset, @Param("limit") int limit);

    List<Item> findByBuyerId(@Param("buyerId") Long buyerId);

    List<Item> findByBuyerPage(@Param("buyerId") Long buyerId, @Param("offset") int offset, @Param("limit") int limit);

    long countAll();

    long countActive();

    long countByBuyerId(@Param("buyerId") Long buyerId);

    Item findById(@Param("id") Long id);

    long countBySellerId(@Param("sellerId") Long sellerId);

    void insert(Item item);

    void update(Item item);

    void markAsSold(@Param("id") Long id, @Param("buyerId") Long buyerId, @Param("tradedAt") java.time.LocalDateTime tradedAt);

    void clearBuyerByUserId(@Param("buyerId") Long buyerId);

    void deleteBySellerId(@Param("sellerId") Long sellerId);

    void deleteById(@Param("id") Long id);
}
