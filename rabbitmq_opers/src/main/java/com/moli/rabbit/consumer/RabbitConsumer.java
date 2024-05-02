package com.moli.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.moli.rabbit.config.RabbitConfig.*;

/**
 * @author moli
 * @time 2024-03-16 16:40:36
 * @description 消费者
 */
@Slf4j
@Component
@RabbitListener(queues = {TEST_QUEUE})
public class RabbitConsumer {

    @RabbitHandler
    public void process(String msg, Message message, Channel channel) throws IOException {
        log.info("消费者收到了消息，消息内容为：{}", msg);
        // 没有进行ack
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
