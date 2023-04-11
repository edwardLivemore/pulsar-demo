package com.example.pulsardemo.config;

import org.apache.pulsar.client.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {
    private static final String TOPIC = "test-topic";

    private static final String SCRIBE = "test-subscription";

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl("pulsar://10.101.12.23:6650")
                .build();
    }

    @Bean
    public Producer<String> stringProducer(PulsarClient client) throws PulsarClientException {
        return client.newProducer(Schema.STRING).topic(TOPIC).create();
    }

    @Bean
    public Consumer<String> consumer(PulsarClient client) throws PulsarClientException {
        return client.newConsumer(Schema.STRING).topic(TOPIC).subscriptionName(SCRIBE).subscribe();
    }
}
