package com.cotato.itda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.cotato.itda")
@EnableScheduling
public class ItdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItdaApplication.class, args);
	}

}
