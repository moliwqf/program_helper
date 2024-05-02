package com.moli.rabbit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * 该项目用来操作rabbitmq
 * 1. 消息丢失情况：
 *      1.1 发送时失败，即：producer -> broker 消息丢失 或者 消息未到达队列
 *      1.2 broker 自身原因导致消息丢失
 *      1.3 消费者消费时造成的消息丢失，即：broker -> consumer
 * 2. 保证消息的可靠性操作
 *      2.1 解决发送时失败 - 使用confirm机制 和 return机制。见：callback.*
 *      2.2 解决broker自身原因导致的消息丢失问题 - 使用持久化机制
 *      2.3 解决消息消息丢失问题 - 将自动ack改为手动ack 'acknowledge-mode: manual'
 *
 */
@SpringBootApplication
public class RabbitmqOpersApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitmqOpersApplication.class, args);
    }

}
