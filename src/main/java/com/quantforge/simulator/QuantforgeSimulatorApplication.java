package com.quantforge.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuantforgeSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuantforgeSimulatorApplication.class, args);
	}

}
