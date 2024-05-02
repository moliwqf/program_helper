package com.moli.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

import static com.moli.rabbit.config.RabbitConfig.DLX_QUEUE;

/**
 * @author moli
 * @time 2024-03-18 14:13:28
 * @description TODO
 */
@Slf4j
@Component
@RabbitListener(queues = {DLX_QUEUE})
public class DlxConsumer {

    @RabbitHandler
    public void handle(String msg, Message message, Channel channel) throws IOException {
        log.info("收到了死信消息：{}, 时间为：{}", msg, new Date());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
