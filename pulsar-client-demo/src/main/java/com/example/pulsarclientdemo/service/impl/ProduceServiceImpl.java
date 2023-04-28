package com.example.pulsarclientdemo.service.impl;

import com.example.pulsarclientdemo.model.PersonInfo;
import com.example.pulsarclientdemo.service.ProduceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProduceServiceImpl implements ProduceService {
    @Autowired
    private Producer<PersonInfo> producer;

    @Override
    public void produce() throws PulsarClientException {
        log.info("test pulsar");

        // 生产消息
        producer.send(new PersonInfo("Edward", 33));
    }
}
