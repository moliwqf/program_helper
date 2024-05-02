package com.moli.rocketmq.example.seq;

import com.moli.rocketmq.entiry.ProducerSingleton;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.apache.rocketmq.client.java.message.MessageBuilderImpl;

import java.io.IOException;


/**
 * @author moli
 * @time 2024-05-01 20:08:08
 * @description 顺序消息生产者
 */
public class ProducerSeqMessageExample {

    public static void main(String[] args) throws ClientException, IOException {
        String topic = "moli_seq_topic";
        String key = "moli_seq_key";
        String tag = "moli_seq_tag";
        String messageGroup = "moli_seq_message_group";

        final Producer producer = ProducerSingleton.getProducer(topic);

        //顺序消息发送。
        MessageBuilder messageBuilder = new MessageBuilderImpl();;
        Message message = messageBuilder.setTopic(topic)
                //设置消息索引键，可根据关键字精确查找某条消息。
                .setKeys(key)
                //设置消息Tag，用于消费端根据指定Tag过滤消息。
                .setTag(tag)
                //设置顺序消息的排序分组，该分组尽量保持离散，避免热点排序分组。
                .setMessageGroup(messageGroup)
                //消息体。
                .setBody("seq message".getBytes())
                .build();
        try {
            //发送消息，需要关注发送结果，并捕获失败等异常
            for (int i = 0; i < 100; i++) {
                SendReceipt sendReceipt = producer.send(message);
                System.out.println(sendReceipt);
            }
        } catch (ClientException e) {
            e.printStackTrace();
        }

        producer.close();
    }
}
