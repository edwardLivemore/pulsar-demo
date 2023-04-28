package com.example.config;

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
                .authentication(AuthenticationFactory.token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaGluYWRlcFJvbGVBIn0.Xk7uGGZ5KKDJ_kN0_kKSl7lHa_3LM8fqb-ijechkA1w"))
                .tlsTrustCertsFilePath(null)
                .allowTlsInsecureConnection(false)
                .build();
    }
}
