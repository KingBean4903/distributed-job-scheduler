package com.example.demo.execution.executor;

import java.time.Duration;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;

import reactor.core.Exceptions;

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
		} catch (WebClientResponseException e) {
			int status = e.getStatusCode().value();
			
			if (e.getStatusCode().is4xxClientError()) {
				return ExecutionResult.failure(
						FailureType.CLIENT_ERROR, 
						status, 
						e.getMessage());
			}
			
			if (e.getStatusCode().is5xxServerError()) {
				return ExecutionResult.failure(
						FailureType.SERVER_ERROR, 
						status, 
						e.getMessage());
			}
			
			
			return ExecutionResult.failure(
					FailureType.UNKNOWN,
					status,
					e.getMessage());
		} catch(WebClientRequestException e) { 
			return classifyRequestFuture(e);
		} catch (Exception e) {
			return ExecutionResult.failure(FailureType.UNKNOWN, null, e.getMessage());
		}
	}
	
	private ExecutionResult classifyRequestFuture(
			WebClientRequestException e) {
		
		Throwable cause =
				Exceptions.unwrap(e);
		
		if (cause instanceof java.util.concurrent.TimeoutException) {
			return ExecutionResult.failure(
					FailureType.TIMEOUT, 
					null, 
					e.getMessage());
		}
		
		return ExecutionResult.failure(
				FailureType.CONNECTION_ERROR, 
				null, 
				e.getMessage());
		
	}

	
}
