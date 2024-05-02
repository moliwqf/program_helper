package com.moli.rocketmq.example.normal;

import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import static com.moli.rocketmq.entiry.ProducerSingleton.CONFIGURATION;

/**
 * @author moli
 * @time 2024-04-30 20:15:02
 * @description 主动拉取消息消费者
 */
public class SimpleNormalMessageExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushNormalMessageExample.class);

    public static void main(String[] args) throws ClientException {

        ClientServiceProvider provider = ClientServiceProvider.loadService();
        String topic = "moli_normal_topic";
        String consumerGroup = "moli_normal_consumer_group_simple";
        String tag = "moli_normal_tag";

        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
        SimpleConsumer simpleConsumer = provider.newSimpleConsumerBuilder()
                // 设置消费者分组。
                .setConsumerGroup(consumerGroup)
                // 设置接入点。
                .setClientConfiguration(CONFIGURATION)
                // 设置预绑定的订阅关系。
                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
                // 设置从服务端接受消息的最大等待时间
                .setAwaitDuration(Duration.ofSeconds(10))
                .build();
        try {
            // SimpleConsumer 需要主动获取消息，并处理。
            List<MessageView> messageViewList = simpleConsumer.receive(5, Duration.ofSeconds(30));
            messageViewList.forEach(messageView -> {
                System.out.println(messageView);
                // 消费处理完成后，需要主动调用 ACK 提交消费结果。
                try {
                    simpleConsumer.ack(messageView);
                } catch (ClientException e) {
                    LOGGER.error("Failed to ack message, messageId={}", messageView.getMessageId(), e);
                }
            });
        } catch (ClientException e) {
            // 如果遇到系统流控等原因造成拉取失败，需要重新发起获取消息请求。
            LOGGER.error("Failed to receive message", e);
        }
        LockSupport.park();
    }
}
