package com.example;

import com.example.pulsar.service.ProduceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PulsarProducerDemoApplication implements ApplicationRunner {
    @Autowired
    private ProduceService produceService;

    public static void main(String[] args) {
        SpringApplication.run(PulsarProducerDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        produceService.produce();
    }
}