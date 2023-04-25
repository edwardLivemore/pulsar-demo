package com.example.pulsarclientdemo.service;

import org.apache.pulsar.client.api.PulsarClientException;

public interface ConsumerService {
    void consume() throws PulsarClientException;
}
