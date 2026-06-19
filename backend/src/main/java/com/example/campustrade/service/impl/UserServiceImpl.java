package com.example.campustrade.service.impl;

import com.example.campustrade.entity.User;
import com.example.campustrade.mapper.ItemMapper;
import com.example.campustrade.mapper.UserMapper;
import com.example.campustrade.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    @Transactional
    public User register(String username, String rawPassword, String phone) {
        User existing = userMapper.findByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setRole("USER");
        userMapper.insert(user);
        log.info("Registered new user username={}", username);
        return user;
    }

    @Override
    public List<User> listAll() {
        return userMapper.findAll();
    }

    @Override
    public List<User> listPage(int page, int size) {
        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * safeSize;
        return userMapper.findPage(offset, safeSize);
    }

    @Override
    public long countAll() {
        return userMapper.countAll();
    }

    @Override
    @Transactional
    public void updateRole(Long userId, String role, User operator) {
        User target = userMapper.findById(userId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        if (!"ADMIN".equals(normalizedRole) && !"USER".equals(normalizedRole)) {
            throw new IllegalArgumentException("无效角色");
        }
        if (operator.getId().equals(userId) && "USER".equals(normalizedRole)) {
            throw new IllegalArgumentException("不能将自己降级为普通用户");
        }
        userMapper.updateRole(userId, normalizedRole);
        log.info("Updated user role userId={} role={} by operator={}", userId, normalizedRole, operator.getId());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, User operator) {
        User target = userMapper.findById(userId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (operator.getId().equals(userId)) {
            throw new IllegalArgumentException("不能删除当前登录管理员");
        }
        long itemCount = itemMapper.countBySellerId(userId);
        if (itemCount > 0) {
            throw new IllegalArgumentException("该用户仍有发布商品，无法删除");
        }
        userMapper.deleteById(userId);
        log.info("Deleted user userId={} by operator={}", userId, operator.getId());
    }

    @Override
    @Transactional
    public void adminDeleteUser(Long userId, User operator) {
        if (!"ADMIN".equalsIgnoreCase(operator.getRole())) {
            throw new IllegalArgumentException("仅管理员可操作");
        }
        User target = userMapper.findById(userId);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (!"USER".equalsIgnoreCase(target.getRole())) {
            throw new IllegalArgumentException("仅可删除普通用户");
        }
        // 先删除该普通用户发布的商品，再清理其作为买家的订单关联，最后删除用户。
        itemMapper.deleteBySellerId(userId);
        itemMapper.clearBuyerByUserId(userId);
        userMapper.deleteById(userId);
        log.info("Admin deleted user userId={} operator={}", userId, operator.getId());
    }
}
