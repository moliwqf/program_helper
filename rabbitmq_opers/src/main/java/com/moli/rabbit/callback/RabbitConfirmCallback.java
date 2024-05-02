package com.moli.rabbit.callback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author moli
 * @time 2024-03-16 16:15:33
 * @description rabbitmq confirm 机制回调 - 消息未到达交换机触发
 */
@Slf4j
@Component
public class RabbitConfirmCallback implements RabbitTemplate.ConfirmCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     * 消息接收回调方法
     * @param correlationData 消息属性体
     * @param ack 是否ack
     * @param cause rabbitmq信息
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        // 如果发送端发送消息没有对 correlationData 进行处理，correlationData 就会为null
        if (!ack && correlationData == null) {
            log.error(cause);
            return;
        }
        if (!ack) {
            log.info("消息id为；{}", correlationData.getId());
            Message message = correlationData.getReturnedMessage();
            assert message != null;
            log.info("消息体为：{}", new String(message.getBody()));
            // 可以进行持久化到数据库中，然后进行补偿处理或者重试操作
            return;
        }
        // 消息接收完毕
        log.info("消息到达了交换机~~~~");
//        log.info(correlationData.toString());
    }
}
