package com.example.pulsar.config;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfig {

    @Value("${pulsar.admin.url:}")
    private String pulsarServiceUrl;

    @Bean
    public PulsarAdmin getPulsarAdmin() throws PulsarClientException {
        return PulsarAdmin.builder()
//                .authentication("com.org.MyAuthPluginClass", )
                .serviceHttpUrl(pulsarServiceUrl)
                .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.EPWQdbkfBQO_6NsG-zYsmqZ_kF6Cfc_kslq_7LF-99M"))
                .tlsTrustCertsFilePath(null)
                .allowTlsInsecureConnection(false)
                .build();
    }
}
