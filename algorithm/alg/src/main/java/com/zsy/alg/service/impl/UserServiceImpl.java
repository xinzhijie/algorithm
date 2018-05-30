package com.zsy.alg.service.impl;

import com.zsy.alg.entity.User;
import com.zsy.alg.mapper.UserMapper;
import com.zsy.alg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper; // 这里会报错，但是并不会影响

    public User getUser(User user) {

        return userMapper.selectByUser(user);
    }
}
