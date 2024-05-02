package com.moli.redis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @author moli
 * @time 2024-03-15 15:21:20
 * @description 订单超时处理器
 */
@Slf4j
@Component
public class OrderPaymentTimeout implements RedisDelayQueueHandler<String>{

    @Override
    public void execute(String orderId) {
        log.info("(收到订单支付超时延迟消息) {}, 时间为：{}", orderId, new Date());

    }
}
