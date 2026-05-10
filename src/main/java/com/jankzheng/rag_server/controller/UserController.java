package com.jankzheng.rag_server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${app.auth.username}")
    private String fixedUsername;

    @Value("${app.auth.password}")
    private String fixedPassword;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        if (fixedUsername.equals(username) && fixedPassword.equals(password)) {
            return Map.of("success", true, "message", "登录成功");
        }
        return Map.of("success", false, "message", "用户名或密码错误");
    }
}
