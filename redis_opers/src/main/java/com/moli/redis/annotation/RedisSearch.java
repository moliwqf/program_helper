package com.moli.redis.annotation;

import org.redisson.api.search.index.IndexType;

import java.lang.annotation.*;

/**
 * @author moli
 * @time 2024-03-22 15:17:16
 * @description redis-search 自定义注解，作用于类上，用于表示类对应的index信息
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisSearch {

    /**
     * 索引信息
     */
    String index();

    /**
     * redis中保存该信息的key的前缀信息
     */
    String prefix() default "";

    /**
     * 索引创建类型
     * @return IndexType
     */
    IndexType indexType();
}
