package com.bss.uis.uiscommonservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class UisCommonServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UisCommonServiceApplication.class, args);
	}

}
