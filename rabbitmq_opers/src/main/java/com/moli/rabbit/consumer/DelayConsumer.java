package com.moli.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

import static com.moli.rabbit.config.RabbitConfig.DELAY_QUEUE;

/**
 * @author moli
 * @time 2024-03-18 13:38:49
 * @description TODO
 */
@Slf4j
@Component
@RabbitListener(queues = {DELAY_QUEUE})
public class DelayConsumer {

    @RabbitHandler
    public void handle(byte[] msg, Message message, Channel channel) throws IOException {
        log.info("接受到了延迟消息：msg = {}", new String(msg));
        log.info("接受延迟消息时间：{}", new Date());
        log.info(message.getMessageProperties().toString());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
