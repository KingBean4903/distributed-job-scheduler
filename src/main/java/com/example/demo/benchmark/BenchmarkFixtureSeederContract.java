package com.example.demo.benchmark;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BenchmarkProperties.class)
public interface BenchmarkFixtureSeederContract {
	void seed(int jobCount);
}
