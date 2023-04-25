package com.example.pulsarclientdemo.config;

import com.example.pulsarclientdemo.model.PersonInfo;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {
    private static final String TOPIC = "test-topic";

    private static final String SCRIBE = "test-subscription";

    @Value("${pulsar.service.url:}")
    private String pulsarUrl;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(pulsarUrl)
                .build();
    }

    @Bean
    public Producer<PersonInfo> producer(PulsarClient client) throws PulsarClientException {
        return client.newProducer(Schema.JSON(PersonInfo.class))
                .topic(TOPIC)
                .accessMode(ProducerAccessMode.Shared)
                .create();
    }

    @Bean
    public Consumer<PersonInfo> consumer(PulsarClient client) throws PulsarClientException {
        return client.newConsumer(Schema.JSON(PersonInfo.class))
                .topic(TOPIC)
                .subscriptionName(SCRIBE)
                .subscriptionType(SubscriptionType.Failover)
                .subscribe();
    }
}
