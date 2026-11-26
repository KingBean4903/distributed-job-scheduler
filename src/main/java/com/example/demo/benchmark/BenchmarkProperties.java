package com.example.demo.benchmark;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(
		prefix = "scheduler.benchmark")
public record BenchmarkProperties(
		boolean enabled,
		int jobCount, 
		Duration timeout,
		Duration pollInterval) {

}
