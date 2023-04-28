package com.example.pulsarclientdemo.service;

import org.apache.pulsar.client.api.PulsarClientException;

public interface ProduceService {
    void produce() throws PulsarClientException;
}
