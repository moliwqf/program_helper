package com.moli.rocketmq.example.transaction;

import com.moli.rocketmq.entiry.ProducerSingleton;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.*;
import org.apache.rocketmq.client.java.message.MessageBuilderImpl;

import java.io.IOException;

/**
 * @author moli
 * @time 2024-05-01 20:23:47
 * @description 发送事务消息
 */
public class ProducerTransactionMessageExample {

    public static void main(String[] args) throws ClientException, IOException {
        String topic = "moli_transaction_topic";
        String key = "moli_transaction_key";
        String tag = "moli_transaction_tag";

        TransactionChecker checker = (msg) -> {
            System.out.println(msg);
            // 检查是否消费成功，返回成功
            return TransactionResolution.COMMIT;
        };

        final Producer producer = ProducerSingleton.getTransactionProducer(checker, topic);
        //开启事务分支。
        final Transaction transaction;
        try {
            transaction = producer.beginTransaction();
        } catch (ClientException e) {
            e.printStackTrace();
            //事务分支开启失败，直接退出。
            return;
        }

        //顺序消息发送。
        MessageBuilder messageBuilder = new MessageBuilderImpl();
        Message message = messageBuilder.setTopic(topic)
                //设置消息索引键，可根据关键字精确查找某条消息。
                .setKeys(key)
                //设置消息Tag，用于消费端根据指定Tag过滤消息。
                .setTag(tag)
                .addProperty("moli", "moli")
                //设置顺序消息的排序分组，该分组尽量保持离散，避免热点排序分组。
                //消息体。
                .setBody("transaction message".getBytes())
                .build();
        try {
            //发送消息，需要关注发送结果，并捕获失败等异常
            SendReceipt sendReceipt = producer.send(message, transaction);
            System.out.println(sendReceipt);

            transaction.commit();
            System.out.println("事务提交");
        } catch (ClientException e) {
            e.printStackTrace();
        }

        producer.close();
    }
}
