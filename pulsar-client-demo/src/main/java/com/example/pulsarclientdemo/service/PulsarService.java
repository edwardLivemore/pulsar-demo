package com.example.pulsarclientdemo.service;

import org.apache.pulsar.client.api.PulsarClientException;

public interface PulsarService {
    void test() throws PulsarClientException;
}
