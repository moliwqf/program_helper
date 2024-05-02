package com.moli.rabbit.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @author moli
 * @time 2024-03-16 16:28:42
 * @description rabbitmq返回值回调 - 消息未到达队列触发
 */
@Slf4j
@Component
public class RabbitReturnCallback implements RabbitTemplate.ReturnCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setReturnCallback(this);
    }


    /**
     * @param message    消息体
     * @param replyCode  返回代码
     * @param replyText  返回文本
     * @param exchange   交换机
     * @param routingKey 发送方定义的路由key
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println(message);
        System.out.println("消息标识：" + message.getMessageProperties().getDeliveryTag());
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("消息：{}", messageBody);
        System.out.println(replyCode);
        System.out.println(replyText);
        System.out.println(exchange);
        System.out.println(routingKey);
    }
}
