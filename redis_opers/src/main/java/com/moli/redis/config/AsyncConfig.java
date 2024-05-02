package com.moli.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author moli
 * @time 2024-03-15 14:28:35
 * @description 异步配置类
 */
@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean(value = {"customThreadPool"})
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(20);
        pool.setQueueCapacity(20);
        pool.setKeepAliveSeconds(60);
        pool.setThreadNamePrefix("async-thread-task-");
        return pool;
    }
}

