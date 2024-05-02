package com.moli.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author moli
 * @time 2024-05-02 10:16:30
 * @description 顺序消费
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "moli_seq_topic",
        consumerGroup = "moli_seq_message_group",
        consumeMode = ConsumeMode.ORDERLY // 有序消费
)
public class PushConsumerSeq implements RocketMQListener<String> {

    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}
