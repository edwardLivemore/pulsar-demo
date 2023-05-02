package com.example.pulsar.service;

import org.apache.pulsar.client.api.PulsarClientException;

public interface ProduceService {
    void produce() throws PulsarClientException;
}
