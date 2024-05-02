package com.moli.rocketmq.entiry;

import org.apache.rocketmq.client.apis.*;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.ProducerBuilder;
import org.apache.rocketmq.client.apis.producer.TransactionChecker;

import java.time.Duration;

public class ProducerSingleton {
    private static volatile Producer PRODUCER;
    private static volatile Producer TRANSACTION_PRODUCER;
    public static final ClientConfiguration CONFIGURATION;

    static {
        CONFIGURATION = ClientConfiguration.newBuilder()
                .setCredentialProvider(
                        new StaticSessionCredentialsProvider("rocketmq2", "12345678")
                )
                .setRequestTimeout(Duration.ofSeconds(3L))
                .setEndpoints("192.168.3.140:8080")
                .enableSsl(false)
                .build();
    }

    public static Producer getTransactionProducer(TransactionChecker checker, String... topics) {
        if (TRANSACTION_PRODUCER == null) {
            synchronized (ProducerSingleton.class) {
                if (TRANSACTION_PRODUCER == null) {
                    TRANSACTION_PRODUCER = buildProducer(checker, topics);
                }
            }
        }
        return TRANSACTION_PRODUCER;
    }

    public static Producer getProducer(String... topics) throws ClientException {
        if (PRODUCER == null) {
            synchronized (ProducerSingleton.class) {
                if (PRODUCER == null) {
                    PRODUCER = buildProducer(null, topics);
                }
            }
        }
        return PRODUCER;
    }

    public static Producer buildProducer(TransactionChecker checker, String... topics) {
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        try {
            ProducerBuilder builder = provider
                    .newProducerBuilder()
                    .setClientConfiguration(CONFIGURATION)
                    .setMaxAttempts(3)
                    .setTopics(topics);
            if (checker != null) {
                builder.setTransactionChecker(checker);
            }
            return builder.build();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        }
    }
}