package com.example.pulsar.config;


import com.example.pulsar.model.PersonInfo;
import org.apache.pulsar.client.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {
    @Value("${pulsar.tenant1.topic}")
    private String topic;

    @Value("${pulsar.service.url:}")
    private String pulsarUrl;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(pulsarUrl)
                .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMS1wcm9kdWNlciJ9.0OoVBSs5ZndG-vytuAybr5edEdD1MpXXApDP9RJJlQI"))
                .tlsTrustCertsFilePath(null)
                .allowTlsInsecureConnection(false)
                .build();
    }

    @Bean
    public Producer<PersonInfo> producer(PulsarClient client) throws PulsarClientException {
        return client.newProducer(Schema.JSON(PersonInfo.class))
                .topic(topic)
                .accessMode(ProducerAccessMode.Shared)
                .create();
    }
}
