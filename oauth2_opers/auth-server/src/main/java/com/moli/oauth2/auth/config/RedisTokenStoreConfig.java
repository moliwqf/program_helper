package com.moli.oauth2.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * @author moli
 * @time 2024-04-11 23:17:41
 * @description redis token store
 */
@Configuration
public class RedisTokenStoreConfig {

    @Resource
    private RedisConnectionFactory connectionFactory;

    @Bean
    public TokenStore redisTokenStore() {
        return new RedisTokenStore(connectionFactory);
    }
}
