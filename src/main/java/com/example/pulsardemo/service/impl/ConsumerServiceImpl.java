package com.example.pulsardemo.service.impl;


import com.example.pulsardemo.service.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerServiceImpl implements ConsumerService {
    @Autowired
    private Consumer<String> consumer;

    @Override
    public void consume() throws PulsarClientException {
        while (true) {
            // Wait for a message
            Message msg = consumer.receive();

            try {
                // Do something with the message
                log.info("Message received: " + new String(msg.getData()));

                // Acknowledge the message so that it can be deleted by the message broker
                consumer.acknowledge(msg);
            } catch (Exception e) {
                // Message failed to process, redeliver later
                e.printStackTrace();
                consumer.negativeAcknowledge(msg);
            }
        }
    }
}
