package com.example;

import com.example.pulsar.service.ConsumerService;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PulsarConsumerDemoApplication implements ApplicationRunner {
    @Autowired
    private ConsumerService consumerService;

    public static void main(String[] args) {
        SpringApplication.run(PulsarConsumerDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动消费者
        new Thread(() -> {
            try {
                consumerService.consume();
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
        }).start();
    }
}