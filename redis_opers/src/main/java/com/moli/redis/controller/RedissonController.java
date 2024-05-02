package com.moli.redis.controller;

import com.moli.redis.constant.RedisDelayQueueEnum;
import com.moli.redis.handler.context.DelayQueueHandlerContext;
import org.redisson.api.RQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author moli
 * @time 2024-03-15 15:12:56
 * @description redisson控制层
 */
@RestController
@RequestMapping("/redisson")
public class RedissonController {

    @Autowired
    @Qualifier("delayQueueHandlerContext")
    private DelayQueueHandlerContext delayQueueHandlerContext;

    // 测试发送延迟消息
    @GetMapping("/testRedisson")
    public String testRedisson() {
        delayQueueHandlerContext.addOneDelayedMsg("超时消息出现", RedisDelayQueueEnum.ORDER_PAYMENT_TIMEOUT, 3);
        return "hello, redisson";
    }
}
