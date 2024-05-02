package com.moli.rocketmq.controller;

import com.moli.rocketmq.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author moli
 * @time 2024-05-02 00:30:42
 * @description 消息发送
 */
@RestController
@Slf4j
@RequestMapping("/msg/send")
public class MessageSendController {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("normal")
    public String sendNormalMsg(@RequestParam("msg") String msg) {
        for (int i = 0; i < 100; i++) {
            rocketMQTemplate.convertAndSend("moli_normal_topic", msg + i);
        }
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }

    @GetMapping("spring")
    public String sendSpringMsg(@RequestParam("msg") String msg) {
        Message<String> message = MessageBuilder.withPayload(msg)
                .setHeader(RocketMQHeaders.KEYS, "moli_normal_key").build();
        rocketMQTemplate.send(
                "moli_normal_topic:moli_normal_tag",
                message
        );
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }

    @GetMapping("async")
    public String sendAsync(@RequestParam("msg") String msg) {
        for (int i = 0; i < 100; i++) {
            rocketMQTemplate.asyncSend("moli_normal_topic", new User(1, msg + i), new SendCallback() {
                @Override
                public void onSuccess(SendResult var1) {
                    System.out.printf("async onSucess SendResult=%s %n", var1);
                }

                @Override
                public void onException(Throwable var1) {
                    System.out.printf("async onException Throwable=%s %n", var1);
                }

            });
        }
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }

    /**
     * 保证发送消息有序：1. 同步发送 2. 消费模式为有序
     */
    @GetMapping("orderly")
    public String sendOrderly(@RequestParam("msg") String msg) {
        for (int i = 0; i < 100; i++) {
            rocketMQTemplate.syncSendOrderly("moli_seq_topic", MessageBuilder.withPayload(msg + i).build(), "moli_seq_message_group");
        }
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }

    @GetMapping("delay")
    public String sendDelay(@RequestParam("msg") String msg) {
        rocketMQTemplate.syncSendDelayTimeSeconds("moli_delay_topic", MessageBuilder.withPayload(msg).build(), 10);
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }

    @GetMapping("transaction")
    public String sendTransaction(@RequestParam("msg") String msg) {
        TransactionSendResult transaction = rocketMQTemplate.sendMessageInTransaction("moli_transaction_topic", MessageBuilder.withPayload(msg).build(), null);
        log.info("发送数据 : " + msg);
        return msg + "发送成功";
    }
}
