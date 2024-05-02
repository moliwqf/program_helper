package com.moli.oauth2.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author moli
 * @time 2024-04-11 23:07:29
 * @description oauth2 server 端
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.moli.oauth2.auth.mapper"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
