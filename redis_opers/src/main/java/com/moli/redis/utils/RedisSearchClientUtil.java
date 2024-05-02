package com.moli.redis.utils;

import io.redisearch.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author moli
 * @time 2024-03-22 15:12:58
 * @description redis-search client 获取
 */
@Component
public class RedisSearchClientUtil {

    private static String host = "192.168.3.136";
    private static String password = null;
    private static int port = 6379;
    private static Duration timeout = Duration.ofSeconds(500L);
    private static int poolSize = 5;
    // 将索引进行缓存
    private static final Map<String, Client> clients = new HashMap<>(16);

    @Value("${spring.redis.host:localhost}")
    public void setHost(String host) {
        if (null == host) {
            host = "192.168.3.136";
        }
        RedisSearchClientUtil.host = host;
    }

    @Value("${spring.redis.password:}")
    public void setPassword(String password) {

        if (StringUtils.isEmpty(password)) {
            password = null;
        }
        RedisSearchClientUtil.password = password;
    }

    @Value("${spring.redis.port:6379}")
    public void setPort(Integer port) {
        RedisSearchClientUtil.port = port;
    }

    @Value("${spring.redis.timeout:500}")
    public void setTimeout(Duration timeout) {
        RedisSearchClientUtil.timeout = timeout;
    }

    @Value("${spring.redis.poolSize:5}")
    public void setPoolSize(Integer poolSize) {
        RedisSearchClientUtil.poolSize = poolSize;
    }

    /**
     * 根据缓存获取连接或者创建新的连接信息
     * @param index redis中的索引 - key
     * @return io.redisearch.client.Client redis服务端连接
     */
    public static Client getClient(String index) {
        if (clients.containsKey(index)) {
            Client client = clients.get(index);
            try {
                Map<String, Object> info = client.getInfo();
                System.out.println(info);
            } catch (Exception e) {
                Client create = createClient(index);
                clients.put(index, create);
                return create;
            }
            return client;
        } else {
            Client create = createClient(index);
            clients.put(index, create);
            return create;
        }
    }

    /**
     * 创建新的redis连接信息
     * @param index 索引
     * @return io.redisearch.client.Client redis服务端连接
     */
    private static Client createClient(String index) {
        int timeoutSecs = (int) timeout.getSeconds();
        return new Client(index, host, port, timeoutSecs, poolSize, password);
    }
}
