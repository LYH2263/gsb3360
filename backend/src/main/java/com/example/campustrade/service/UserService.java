package com.example.campustrade.service;

import com.example.campustrade.entity.User;

import java.util.List;

public interface UserService {

    User findByUsername(String username);

    User findById(Long id);

    User register(String username, String rawPassword, String phone);

    List<User> listAll();

    List<User> listPage(int page, int size);

    long countAll();

    void updateRole(Long userId, String role, User operator);

    void deleteUser(Long userId, User operator);

    void adminDeleteUser(Long userId, User operator);
}
