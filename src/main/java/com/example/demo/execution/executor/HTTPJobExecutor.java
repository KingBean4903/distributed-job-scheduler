package com.example.demo.execution.executor;

import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;

@Component
public class HTTPJobExecutor implements JobExecutor {

	private final WebClient webClient;
	
	public HTTPJobExecutor(
			WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder.build();
	}

	@Override
	public boolean supports(JobType jobType) {
		return jobType == JobType.HTTP;
	}

	@Override
	public ExecutionResult execute(Job job, JobExecution executor) {
		try {
			
			webClient
					.method(HttpMethod.GET)
					.uri(job.getPayload())
					.retrieve()
					.toBodilessEntity()
					.block(Duration.ofSeconds(
							job.getTimeoutSeconds()));
			
			return ExecutionResult.succeed();
		} catch (Exception e) {
			return ExecutionResult.failure(e.getMessage());
		}
	}
	
	
	
}
