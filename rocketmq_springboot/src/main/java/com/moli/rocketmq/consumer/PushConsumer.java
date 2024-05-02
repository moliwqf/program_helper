package com.moli.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author moli
 * @time 2024-05-01 21:20:04
 * @description 普通消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "moli_normal_topic",
        consumerGroup = "moli_normal_consumer_group",
        enableMsgTrace = true,
        customizedTraceTopic = "trace-topic",
        selectorExpression = "moli_normal_tag"
)
public class PushConsumer implements RocketMQListener<String> {

    public void onMessage(String message) {
        log.info("received message: {}", message);
    }
}
