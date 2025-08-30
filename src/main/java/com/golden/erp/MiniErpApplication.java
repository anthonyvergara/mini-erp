package com.golden.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableFeignClients
@EnableRetry
public class MiniErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniErpApplication.class, args);
	}

}
