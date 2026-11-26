package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.demo.benchmark.BenchmarkProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BenchmarkProperties.class)
public class DistributedJobSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedJobSchedulerApplication.class, args);
	}

}
