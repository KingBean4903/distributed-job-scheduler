package com.example.demo.scheduler.execution.executor.config;

import java.time.Clock;
import java.time.Instant;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestClockConfig {
	
	@Bean
	public MutableClock mutableClock() {
		return new MutableClock(
				Instant.parse("2026-10-02T09:00:00Z"));
	}
	
	@Bean
	@Primary
	public Clock clock(MutableClock mutableClock) {
		return mutableClock;
	}
	

}
