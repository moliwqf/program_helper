package com.moli.redis.handler.context;

import com.moli.redis.constant.RedisDelayQueueEnum;
import com.moli.redis.handler.RedisDelayQueueHandler;
import com.moli.redis.utils.RedisDelayQueueUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author moli
 * @time 2024-03-18 21:55:25
 * @description 延迟队列处理器上下文
 */
@Slf4j
@Component
public class DelayQueueHandlerContext {

    @Resource
    private Map<String, RedisDelayQueueHandler> handlers;

    @Resource
    private RedisDelayQueueUtil redisDelayQueueUtil;

    public void addOneDelayedMsg(String msg, RedisDelayQueueEnum delayQueueEnum, long delayTime) {
        log.info("向{}中添加了一条延迟消息，消息体为：{}, 当前时间为：{}", delayQueueEnum.getName(), msg, new Date());
        redisDelayQueueUtil.addDelayQueue(msg, delayTime, TimeUnit.SECONDS, delayQueueEnum.getCode());
    }
}
