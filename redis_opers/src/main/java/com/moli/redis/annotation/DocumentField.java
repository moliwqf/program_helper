package com.moli.redis.annotation;

import com.moli.redis.enums.RedisSearchFieldType;

import java.lang.annotation.*;

/**
 * @author moli
 * @time 2024-03-22 15:27:05
 * @description 搜索实例字段上使用
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DocumentField {

    /**
     * 当前字段是否为主键
     */
    boolean id() default false;
    /**
     * 字段类型
     */
    RedisSearchFieldType fieldType() default RedisSearchFieldType.AUTO;

    /**
     * 字段名
     */
    String fieldName() default "";

    /**
     * 描述信息
     */
    String desc() default "";

    /**
     * 是否存在
     */
    boolean exist() default true;
}
