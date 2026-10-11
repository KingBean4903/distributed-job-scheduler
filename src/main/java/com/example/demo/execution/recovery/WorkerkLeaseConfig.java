package com.example.demo.execution.recovery;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerkLeaseConfig {
	
	@Bean
	public Duration workerLeaseDuration(
			@Value("${scheduler.worker.lease-duration-seconds:1}")
			long seconds) {
		
		if (seconds <= 0) {
			throw new IllegalArgumentException("scheduler.worker.lease-duration-seconds must be greater than 0");
		}
		
		
		return Duration.ofSeconds(seconds);
	}
}
