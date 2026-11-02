package com.smartbook;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableScheduling
@SpringBootApplication
public class SmartbookApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartbookApplication.class, args);
	}

}
