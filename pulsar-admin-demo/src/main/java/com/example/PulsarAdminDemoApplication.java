package com.example;

import com.example.pulsar.service.PulsarAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PulsarAdminDemoApplication implements ApplicationRunner {
    @Autowired
    private PulsarAdminService adminService;

    public static void main(String[] args) {
        SpringApplication.run(PulsarAdminDemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        adminService.init();
    }
}