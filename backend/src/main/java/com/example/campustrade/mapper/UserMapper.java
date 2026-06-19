package com.example.campustrade.mapper;

import com.example.campustrade.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User findByUsername(@Param("username") String username);

    User findById(@Param("id") Long id);

    void insert(User user);

    List<User> findAll();

    List<User> findPage(@Param("offset") int offset, @Param("limit") int limit);

    long countAll();

    void updateRole(@Param("id") Long id, @Param("role") String role);

    void deleteById(@Param("id") Long id);
}
