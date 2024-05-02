package com.moli.rabbit.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author moli
 * @time 2024-03-16 16:07:07
 * @description rabbitmq配置类
 */
@Configuration
public class RabbitConfig {
    public static final String TEST_QUEUE = "rabbit.test.queue";
    public static final String TEST_EXCHANGE = "rabbit.test.exchange";

    public static final String DELAY_QUEUE = "rabbit.delay.queue";
    public static final String DELAY_EXCHANGE = "rabbit.delay.exchange";
    public static final String DELAY_EXCHANGE_TYPE = "x-delayed-message";
    public static final String DELAY_ROUTING_KEY = "rabbit.delay.routing.key";

    public static final String DEAD_DELAY_QUEUE = "rabbit.dead.delay.queue";
    public static final String DEAD_DELAY_EXCHANGE = "rabbit.dead.delay.exchange";
    public static final String DEAD_DELAY_ROUTING_KEY = "rabbit.dead.delay.routing.key";

    public static final String DLX_QUEUE = "rabbit.dlx.queue";
    public static final String DLX_EXCHANGE = "rabbit.dlx.exchange";
    public static final String DLX_ROUTING_KEY = "rabbit.dlx.routing.key";

    @Bean
    public Queue queue() {
        return new Queue(TEST_QUEUE, true);
    }

    @Bean
    public FanoutExchange exchange() {
        return new FanoutExchange(TEST_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingChatLoginMessageDirect() {
        return BindingBuilder.bind(queue()).to(exchange());
    }

    @Bean
    public Queue delayedQueue() {
        return new Queue(DELAY_QUEUE, true, false, false);
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE, DELAY_EXCHANGE_TYPE, true, false, args);
    }

    @Bean
    public Binding bindingDelayQueueToExchange() {
        return BindingBuilder
                .bind(delayedQueue())
                .to(delayedExchange())
                .with(DELAY_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingDlxQueueToExchange() {
        return BindingBuilder
                .bind(dlxQueue())
                .to(dlxExchange())
                .with(DLX_ROUTING_KEY);
    }


    @Bean
    public Queue deadDelayedQueue() {
        Map<String, Object> args = new HashMap<>();
        //设置消息过期时间
        args.put("x-message-ttl", 1000 * 10);
        //设置死信交换机
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        //设置死信 routing_key
        args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
        return new Queue(DEAD_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange deadDelayedExchange() {
        return new DirectExchange(DEAD_DELAY_EXCHANGE, true, false);
    }

    @Bean
    public Binding bindingDeadDelayQueueToExchange() {
        return BindingBuilder
                .bind(deadDelayedQueue())
                .to(deadDelayedExchange())
                .with(DEAD_DELAY_ROUTING_KEY);
    }
}
