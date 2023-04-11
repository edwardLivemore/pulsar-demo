package com.example.pulsardemo;

import com.example.pulsardemo.service.ConsumerService;
import com.example.pulsardemo.service.PulsarService;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PulsarDemoApplication implements ApplicationRunner {
	@Autowired
	private PulsarService pulsarService;

	@Autowired
	private ConsumerService consumerService;

	public static void main(String[] args) {
		SpringApplication.run(PulsarDemoApplication.class, args);
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
		pulsarService.test();
	}
}
