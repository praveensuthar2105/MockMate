package com.mockmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MockmateBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MockmateBackendApplication.class, args);
	}

}