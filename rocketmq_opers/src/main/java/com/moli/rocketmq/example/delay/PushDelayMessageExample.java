package com.moli.rocketmq.example.delay;

import com.moli.rocketmq.example.normal.PushNormalMessageExample;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.locks.LockSupport;

import static com.moli.rocketmq.entiry.ProducerSingleton.CONFIGURATION;

/**
 * @author moli
 * @time 2024-04-29 22:37:51
 * @description 消费延迟消息
 */
public class PushDelayMessageExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushNormalMessageExample.class);

    public static void main(String[] args) throws InterruptedException, IOException, ClientException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();

        String tag = "moli_delay_tag";
        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
        String consumerGroup = "moli_delay_consumer_group";
        String topics = "moli_delay_topic";


        // 创建消费者
        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
                .setClientConfiguration(CONFIGURATION)
                // 设置消费者组
                .setConsumerGroup(consumerGroup)
                // 设置订阅信息
                .setSubscriptionExpressions(Collections.singletonMap(topics, filterExpression))
                // 消息监听回调
                .setMessageListener(messageView -> {
                    // 处理消息
                    LOGGER.info("Consume message={}", messageView);
                    return ConsumeResult.SUCCESS;
                })
                .build();
        Thread.sleep(Long.MAX_VALUE);
        LockSupport.park();
        // 关闭消费者
        pushConsumer.close();
    }
}
