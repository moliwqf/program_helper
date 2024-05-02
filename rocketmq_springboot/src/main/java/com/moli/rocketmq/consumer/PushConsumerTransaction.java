package com.moli.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author moli
 * @time 2024-05-01 21:20:04
 * @description 普通消费者
 */
@Slf4j
@Component
@RocketMQTransactionListener
@RocketMQMessageListener(topic = "moli_transaction_topic", consumerGroup = "moli_transaction_consumer_group")
public class PushConsumerTransaction implements RocketMQLocalTransactionListener, RocketMQListener<String> {

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        log.info("exec local transaction");
        // ... local transaction process, return bollback, commit or unknown
        return RocketMQLocalTransactionState.UNKNOWN;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        log.info("exec check local transaction");
        // ... check transaction status and return bollback, commit or unknown
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public void onMessage(String message) {
        log.info("事务消息:{}", message);
    }
}
