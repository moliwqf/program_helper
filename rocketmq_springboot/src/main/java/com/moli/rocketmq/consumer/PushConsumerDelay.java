package com.moli.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author moli
 * @time 2024-05-01 21:20:04
 * @description 消费延迟消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "moli_delay_topic",
        consumerGroup = "moli_delay_consumer_group"
)
public class PushConsumerDelay implements RocketMQListener<String> {

    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}
