package com.moli.rabbit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import static com.moli.rabbit.config.RabbitConfig.*;


/**
 * @author moli
 * @time 2024-03-16 16:37:56
 * @description 测试控制层
 */
@Slf4j
@RestController
public class RabbitTestController {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/rabbit/test")
    public String sendMsg(String msg) {
        CorrelationData data = new CorrelationData(msg);
        data.setReturnedMessage(new Message(msg.getBytes(StandardCharsets.UTF_8), new MessageProperties()));
        rabbitTemplate.convertAndSend(TEST_EXCHANGE, "*", msg, data);
        return "发送成功，消息体为：" + msg;
    }

    @GetMapping("/rabbit/delay")
    public String sendDelayMsg(String msg) {
        /*CorrelationData data = new CorrelationData(msg);
        Message message = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8)).build();
        data.setReturnedMessage(message);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, DELAY_ROUTING_KEY, msg, msgProcessor -> {
            // 延迟10s
            long delayTime = 10 * 1000L;
            msgProcessor.getMessageProperties().setDelay(Math.toIntExact(delayTime));
            return msgProcessor;
        }, data);
        log.info("发送延迟消息时间：{}", new Date());*/
        CorrelationData data = new CorrelationData(msg);
        Message message = MessageBuilder.withBody(msg.getBytes(StandardCharsets.UTF_8)).setHeader("x-delay", 10000).build();
        data.setReturnedMessage(message);
        rabbitTemplate.convertAndSend(DELAY_EXCHANGE, DELAY_ROUTING_KEY, message, data);
        log.info("发送延迟消息时间：{}", new Date());

        return "发送延迟消息成功，消息体为：" + msg;
    }

    @GetMapping("/rabbit/delay/dlx")
    public String sendDelayMsgByDlx(String msg) {
        CorrelationData data = new CorrelationData(msg);
        Message message = new Message(msg.getBytes(StandardCharsets.UTF_8), new MessageProperties());
        data.setReturnedMessage(message);
        rabbitTemplate.convertAndSend(DEAD_DELAY_EXCHANGE, DEAD_DELAY_ROUTING_KEY, msg, data);
        log.info("发送延迟消息时间：{}", new Date());
        return "发送延迟消息成功，消息体为：" + msg;
    }
}
