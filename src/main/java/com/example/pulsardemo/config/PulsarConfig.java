package com.example.pulsardemo.config;

import com.example.pulsardemo.model.PersonInfo;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {
    private static final String TOPIC = "json-topic";

    private static final String SCRIBE = "json-subscription";

    @Value("${pulsar.url:}")
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
