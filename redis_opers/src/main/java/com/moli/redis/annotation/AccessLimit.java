package com.moli.redis.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author moli
 * @time 2024-03-05 11:16:27
 * @description 访问限流注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AccessLimit {

    /**
     * 名字
     */
    String name() default "";

    /**
     * redis中的key
     */
    String key() default "";

    /**
     * key的前缀
     */
    String prefix() default "";

    /**
     * 时间段内
     */
    int second();

    /**
     * 请求次数
     */
    int count();

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 使用的key的类型
     */
    LimitKeyType keyType() default LimitKeyType.METHOD;

    enum LimitKeyType {
        /**
         * 自定义key类型
         */
        CUSTOMER,

        /**
         * ip作为限流的key值
         */
        IP,

        /**
         * controller + methodName 作为key
         */
        METHOD
    }
}
