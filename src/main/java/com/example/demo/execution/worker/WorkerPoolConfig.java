package com.example.demo.execution.worker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerPoolConfig {
	
	@Bean(destroyMethod = "shutdown")
	public ExecutorService workerExecutor(
			@Value("${scheduler.worker.threads:4}")
			int threads) {
		return Executors.newFixedThreadPool(threads);
	}
}
