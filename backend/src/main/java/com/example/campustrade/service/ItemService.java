package com.example.campustrade.service;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;

import java.util.List;

public interface ItemService {

    List<Item> listAll();

    List<Item> listPage(int page, int size);

    List<Item> listActivePage(int page, int size);

    List<Item> listBySellerId(Long sellerId);

    List<Item> listBySellerPage(Long sellerId, int page, int size);

    List<Item> listByBuyerId(Long buyerId);

    List<Item> listByBuyerPage(Long buyerId, int page, int size);

    long countAll();

    long countActive();

    long countBySellerId(Long sellerId);

    long countByBuyerId(Long buyerId);

    Item getById(Long id);

    void create(Item item, User currentUser);

    void update(Item item, User currentUser);

    void deleteById(Long id, User currentUser);

    void adminDeleteById(Long id, User operator);

    void completeTrade(Long id, User buyer);
}
