package com.moli.redis.query;
import java.util.function.Function;

import java.io.Serializable;

/**
 * @author moli
 * @time 2024-03-22 15:32:05
 * @description 此类定义 是为了 让 方法可以序列化
 */
@FunctionalInterface
public interface RedisFunction<T, R> extends Function<T, R>, Serializable {
}
