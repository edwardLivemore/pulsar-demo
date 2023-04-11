package com.example.pulsardemo.service.impl;

import com.example.pulsardemo.service.PulsarService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PulsarServiceImpl implements PulsarService {
    @Autowired
    private Producer<String> producer;

    @Override
    public void test() throws PulsarClientException {
        log.info("test pulsar");

        // 生产消息
        producer.send("hello edward");
    }
}
