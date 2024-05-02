package com.moli.oauth2.resource.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moli
 * @time 2024-04-12 16:29:39
 * @description 测试
 */
@RestController
public class HomeController {

    @GetMapping("/users")
    public Map<String, Object> test(Authentication authentication) {
        Map<String, Object> data = new HashMap<>(1);
        data.put("user", authentication.getPrincipal());
        return data;
    }
}
