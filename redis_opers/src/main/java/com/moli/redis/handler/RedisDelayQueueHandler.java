package com.moli.redis.handler;

/**
 * @author moli
 * @time 2024-03-15 15:20:21
 * @description redis延迟队列处理器
 */
public interface RedisDelayQueueHandler<T> {

    void execute(T t);
}
