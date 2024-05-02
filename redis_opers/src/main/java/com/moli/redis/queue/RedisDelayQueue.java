package com.moli.redis.queue;

import com.google.common.collect.ImmutableList;
import com.moli.redis.interceptors.AccessLimitInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author moli
 * @time 2024-03-15 14:09:40
 * @description 实现延时队列
 */
//@Component
public class RedisDelayQueue implements Runnable, InitializingBean {

    public static final String DELAY_QUEUE_KEY = "delay.queue";

    public static final Logger logger = LoggerFactory.getLogger(AccessLimitInterceptor.class);

    private final RedisTemplate<String, Object> redisTemplate;

    private final TaskExecutor taskExecutor;

    public RedisDelayQueue(RedisTemplate<String, Object> redisTemplate, @Autowired @Qualifier("customThreadPool") TaskExecutor taskExecutor) {
        this.redisTemplate = redisTemplate;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        while (true) {
            long currentTime = System.currentTimeMillis();
            String key = String.valueOf(currentTime);
//            commonDelayMethod(key);
            luaDelayMethod(key);
        }
    }

    /**
     * 常规方式实现延迟队列
     */
    public void commonDelayMethod(long score) {
        Set<Object> values = redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE_KEY, score, score, 0, 1);
        if (CollectionUtils.isEmpty(values)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 移除对应的信息
            List<String> targetStrings = values.stream().map(Object::toString).collect(Collectors.toList());
            Long remove = redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, targetStrings.get(0));
            if (remove != null && remove > 0) {
                String target = targetStrings.get(0);
                logger.info("target string = {}", target);
                this.handlerMsg(target);
            }
        }
    }

    /**
     * lua脚本实现延迟队列
     */
    public void luaDelayMethod(String key) {
        // 使用lua脚本进行优化
        String script = buildLuaScript();
        RedisScript<String> redisScript = new DefaultRedisScript<>(script, String.class);

        ImmutableList<String> keys = ImmutableList.of(DELAY_QUEUE_KEY);
        String value = redisTemplate.execute(redisScript, keys, String.valueOf(key));
        this.handlerMsg(value);
    }

    private void handlerMsg(String target) {
        System.out.println(target);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        taskExecutor.execute(this);
    }

    private String buildLuaScript() {
        return "local c;" +
                "\nc = redis.call('zrangbyscore',KEYS[1], 0, ARGV[1], 'limit', 0, 1)" +
                // 如果redis中存储的value超过最大值，则直接返回
                "\nif c ~= nil and #c ~= 0 then" +
                "\n  if redis.call('zrem', KEYS[1], c[1]) > 0 then" +
                "\n      return c[1]" +
                "\n  else return '' end" +
                "\nelse return '' end";
    }
}
