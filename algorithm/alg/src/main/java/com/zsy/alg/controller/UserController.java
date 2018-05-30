package com.zsy.alg.controller;

import com.zsy.alg.entity.User;
import com.zsy.alg.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/getUser")
    public Map getUser(User user) {
        Map map = new HashMap();
        User userTemp = userService.getUser(user);
        if (userTemp != null){
            map.put("data", userTemp);
            map.put("stat",true);
        } else {
            map.put("data", "");
            map.put("status",false);
        }
        return map;
    }
}
