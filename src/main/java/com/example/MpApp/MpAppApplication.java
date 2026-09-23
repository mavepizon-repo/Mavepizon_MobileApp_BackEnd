package com.example.MpApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MpAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MpAppApplication.class, args);
	}

}
