package com.moli.rocketmq.example.normal;

import com.moli.rocketmq.entiry.ProducerSingleton;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/**
 * @author moli
 * @time 2024-04-29 22:04:37
 * @description 发送同步普通消息
 */
public class ProducerNormalMessageExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerNormalMessageExample.class);

    public static void main(String[] args) throws ClientException, IOException {
        final ClientServiceProvider provider = ClientServiceProvider.loadService();
        String topics = "moli_normal_topic";
        String keys = "moli_normal_key";
        String tag = "moli_normal_tag";

        final Producer producer = ProducerSingleton.getProducer(topics);
        // 定义消息体
        byte[] body = "This is a normal message for Apache RocketMQ".getBytes(StandardCharsets.UTF_8);

        final Message message = provider.newMessageBuilder()
                // 为当前消息设置主题
                .setTopic(topics)
                // 消息过滤 tag
                .setTag(tag)
                // 消息过滤 key
                .setKeys(keys)
                // 设置消息投递时间戳
//                .setDeliveryTimestamp(timestamp)
                // 设置消息体
                .setBody("body".getBytes())
                // 构建
                .build();
        try {
            for (int i = 0; i < 10; i++) {
                final SendReceipt sendReceipt = producer.send(message);
                LOGGER.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to send message", t);
        }
        producer.close();
    }

}
