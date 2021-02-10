package com.bss.uis.shelter.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ShelterManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShelterManagementServiceApplication.class, args);
    }

}
