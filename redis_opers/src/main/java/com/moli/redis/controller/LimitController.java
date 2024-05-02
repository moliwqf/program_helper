package com.moli.redis.controller;

import com.moli.redis.annotation.AccessLimit;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author moli
 * @time 2024-03-05 14:00:38
 * @description 限流控制层
 */
@RestController
public class LimitController {

    @AccessLimit(second = 10, count = 3)
    @RequestMapping("testLimit1")
    public String testLimit1() {
        return "成功";
    }

    @AccessLimit(second = 10, count = 3, keyType = AccessLimit.LimitKeyType.IP)
    @RequestMapping("testLimit2")
    public String testLimit2() {
        return "成功";
    }
}
