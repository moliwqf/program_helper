package com.moli.rocketmq.example.delay;

import com.moli.rocketmq.entiry.ProducerSingleton;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.message.MessageBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author moli
 * @time 2024-05-01 19:41:30
 * @description 发送延迟消息
 */
public class ProducerDelayMessageExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerDelayMessageExample.class);

    public static void main(String[] args) throws ClientException, IOException {
        //定时/延时消息发送
        MessageBuilder messageBuilder = new MessageBuilderImpl();
        //以下示例表示：延迟时间为10分钟之后的Unix时间戳。
        String topic = "moli_delay_topic";
        String key = "moli_delay_key";
        String tag = "moli_delay_tag";

        final Producer producer = ProducerSingleton.getProducer(topic);

        Long deliverTimeStamp = System.currentTimeMillis() + 1000 * 2;
        Message message = messageBuilder.setTopic(topic)
                //设置消息索引键，可根据关键字精确查找某条消息。
                .setKeys(key)
                //设置消息Tag，用于消费端根据指定Tag过滤消息。
                .setTag(tag)
                .setDeliveryTimestamp(deliverTimeStamp)
                //消息体
                .setBody("delay message".getBytes())
                .build();
        try {
            //发送消息，需要关注发送结果，并捕获失败等异常。
            SendReceipt sendReceipt = producer.send(message);
            LOGGER.info(sendReceipt.toString());
        } catch (ClientException e) {
            e.printStackTrace();
        }
        producer.close();
    }
}
