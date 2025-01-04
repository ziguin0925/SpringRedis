package org.example.waitingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WaitingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaitingApiApplication.class, args);
	}

}
