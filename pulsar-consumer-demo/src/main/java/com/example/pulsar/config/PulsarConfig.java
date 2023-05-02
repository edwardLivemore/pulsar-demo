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

    private static final String SCRIBE = "failover";

    @Value("${pulsar.service.url:}")
    private String pulsarUrl;

    @Bean
    public PulsarClient pulsarClient() throws PulsarClientException {
        return PulsarClient.builder()
                .serviceUrl(pulsarUrl)
                .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"))
                .tlsTrustCertsFilePath(null)
                .allowTlsInsecureConnection(false)
                .build();
    }

    @Bean
    public Consumer<PersonInfo> consumer(PulsarClient client) throws PulsarClientException {
        return client.newConsumer(Schema.JSON(PersonInfo.class))
                .topic(topic)
                .subscriptionName(SCRIBE)
                .subscriptionType(SubscriptionType.Failover)
                .subscribe();
    }
}
