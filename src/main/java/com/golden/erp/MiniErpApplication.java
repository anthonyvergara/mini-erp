package com.golden.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableRetry
@EnableJpaAuditing
@EnableScheduling
public class MiniErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniErpApplication.class, args);
	}

}
