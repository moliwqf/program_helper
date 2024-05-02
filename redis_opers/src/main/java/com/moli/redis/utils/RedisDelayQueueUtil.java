package com.moli.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author moli
 * @time 2024-03-15 15:16:04
 * @description redis延迟队列工具类
 */
@Slf4j
@Component
public class RedisDelayQueueUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 添加延迟队列
     *
     * @param value     队列值
     * @param delay     延迟时间
     * @param timeUnit  时间单位
     * @param queueCode 队列键
     */
    public <T> boolean addDelayQueue(T value, long delay, TimeUnit timeUnit, String queueCode) {
        if (StringUtils.isBlank(queueCode)) {
            return false;
        }
        try {
            RBlockingDeque<T> blockingDeque = redissonClient.getBlockingDeque(queueCode);
            RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            delayedQueue.offer(value, delay, timeUnit);
            log.info("(添加延时队列成功) 队列键：{}，队列值：{}，延迟时间：{}", queueCode, value, timeUnit.toSeconds(delay) + "秒");
        } catch (Exception e) {
            log.error("(添加延时队列失败) {}", e.getMessage());
            throw new RuntimeException("(添加延时队列失败)");
        }
        return true;
    }

    /**
     * 获取延迟队列
     *
     * @param queueCode 延迟队列code
     */
    public <T> T getDelayQueue(String queueCode) throws InterruptedException {
        if (StringUtils.isBlank(queueCode)) {
            return null;
        }
        RBlockingDeque<T> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        return blockingDeque.take();
    }

    /**
     * 删除指定队列中的消息
     *
     * @param t         指定删除的消息对象队列值(同队列需保证唯一性)
     * @param queueCode 指定队列键
     */
    public <T> boolean removeDelayedQueue(T t, String queueCode) {
        if (StringUtils.isBlank(queueCode)) {
            return false;
        }
        RBlockingDeque<T> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<T> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        return delayedQueue.remove(t);
    }
}
