package com.umr.apitesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.umr.apitesting")
public class ApiTestingApplication {
	public static void main(String[] args) {
		SpringApplication.run(ApiTestingApplication.class, args);
	}
}