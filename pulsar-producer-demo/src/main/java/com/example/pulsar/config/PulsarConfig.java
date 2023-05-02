package com.example.pulsar.config;


import com.example.pulsar.model.PersonInfo;
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
                .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaGluYWRlcCJ9.3R77tCfJbfMV3fAdKQrssNOW8j4auYuDUURxkFUjxxA"))
                .build();
    }

    @Bean
    public Producer<PersonInfo> producer(PulsarClient client) throws PulsarClientException {
        return client.newProducer(Schema.JSON(PersonInfo.class))
                .topic(TOPIC)
                .accessMode(ProducerAccessMode.Shared)
                .create();
    }
}
