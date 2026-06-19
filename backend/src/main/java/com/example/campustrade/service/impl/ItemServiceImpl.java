package com.example.campustrade.service.impl;

import com.example.campustrade.entity.Item;
import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import com.example.campustrade.mapper.UserMapper;
import com.example.campustrade.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public List<Item> listAll() {
        return itemMapper.findAll();
    }

    @Override
    public List<Item> listPage(int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * safeSize;
        return itemMapper.findPage(offset, safeSize);
    }

    @Override
    public List<Item> listActivePage(int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * safeSize;
        return itemMapper.findActivePage(offset, safeSize);
    }

    @Override
    public List<Item> listBySellerId(Long sellerId) {
        return itemMapper.findBySellerId(sellerId);
    }

    @Override
    public List<Item> listBySellerPage(Long sellerId, int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * safeSize;
        return itemMapper.findBySellerPage(sellerId, offset, safeSize);
    }

    @Override
    public List<Item> listByBuyerId(Long buyerId) {
        return itemMapper.findByBuyerId(buyerId);
    }

    @Override
    public List<Item> listByBuyerPage(Long buyerId, int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * safeSize;
        return itemMapper.findByBuyerPage(buyerId, offset, safeSize);
    }

    @Override
    public long countAll() {
        return itemMapper.countAll();
    }

    @Override
    public long countActive() {
        return itemMapper.countActive();
    }

    @Override
    public long countBySellerId(Long sellerId) {
        return itemMapper.countBySellerId(sellerId);
    }

    @Override
    public long countByBuyerId(Long buyerId) {
        return itemMapper.countByBuyerId(buyerId);
    }

    @Override
    public Item getById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    @Transactional
    public void create(Item item, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("管理员账号不可发布商品");
        }
        item.setCreatedAt(LocalDateTime.now());
        item.setTradedAt(null);
        item.setBuyerId(null);
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus("ACTIVE");
        }
        // 发布商品必须绑定为当前登录用户
        item.setSellerId(currentUser.getId());
        itemMapper.insert(item);
        log.info("Created item title={} sellerId={}", item.getTitle(), item.getSellerId());
    }

    @Override
    @Transactional
    public void update(Item item, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("管理员账号不可修改商品");
        }
        Item existing = itemMapper.findById(item.getId());
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin && (existing.getSellerId() == null || !existing.getSellerId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("无权限修改他人商品");
        }
        if ("SOLD".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("已卖出商品不可编辑");
        }
        // 防止通过表单篡改 sellerId / createdAt
        item.setSellerId(existing.getSellerId());
        item.setCreatedAt(existing.getCreatedAt());
        item.setBuyerId(existing.getBuyerId());
        item.setTradedAt(existing.getTradedAt());
        if (item.getStatus() == null || item.getStatus().isBlank()) {
            item.setStatus(existing.getStatus() == null || existing.getStatus().isBlank() ? "ACTIVE" : existing.getStatus());
        }
        itemMapper.update(item);
        log.info("Updated item id={} by userId={}", item.getId(), currentUser.getId());
    }

    @Override
    @Transactional
    public void deleteById(Long id, User currentUser) {
        if ("ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new IllegalArgumentException("管理员账号不可删除商品");
        }
        Item existing = itemMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin && (existing.getSellerId() == null || !existing.getSellerId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("无权限删除他人商品");
        }
        if ("SOLD".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("已卖出商品不可删除");
        }
        itemMapper.deleteById(id);
        log.info("Deleted item id={} by userId={}", id, currentUser.getId());
    }

    @Override
    @Transactional
    public void adminDeleteById(Long id, User operator) {
        if (!"ADMIN".equalsIgnoreCase(operator.getRole())) {
            throw new AccessDeniedException("仅管理员可操作");
        }
        Item existing = itemMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (existing.getSellerId() == null) {
            throw new IllegalArgumentException("商品卖家信息缺失");
        }
        if ("SOLD".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("已卖出商品不可删除");
        }
        User seller = userMapper.findById(existing.getSellerId());
        if (seller == null) {
            throw new IllegalArgumentException("卖家不存在");
        }
        if (!"USER".equalsIgnoreCase(seller.getRole())) {
            throw new IllegalArgumentException("仅可删除普通用户发布的商品");
        }
        itemMapper.deleteById(id);
        log.info("Admin deleted item id={} operator={}", id, operator.getId());
    }

    @Override
    @Transactional
    public void completeTrade(Long id, User buyer) {
        if ("ADMIN".equalsIgnoreCase(buyer.getRole())) {
            throw new IllegalArgumentException("管理员账号不可购买商品");
        }
        Item existing = itemMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (existing.getSellerId() != null && existing.getSellerId().equals(buyer.getId())) {
            throw new IllegalArgumentException("不能购买自己发布的商品");
        }
        if (!"ACTIVE".equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalArgumentException("当前商品不可交易");
        }
        itemMapper.markAsSold(id, buyer.getId(), LocalDateTime.now());
        log.info("Trade completed itemId={} buyerId={}", id, buyer.getId());
    }
}
