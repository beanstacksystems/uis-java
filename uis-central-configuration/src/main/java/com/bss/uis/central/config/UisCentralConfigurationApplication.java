package com.bss.uis.central.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
//@EnableDiscoveryClient
@SpringBootApplication
public class UisCentralConfigurationApplication {

    public static void main(String[] args) {
        SpringApplication.run(UisCentralConfigurationApplication.class, args);
    }

}
