package com.example.demo.scheduler.execution.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.JobAttributes;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.retry.ExponentialBackoffCalculator;
import com.example.demo.execution.retry.ExponentialBackoffRetryPolicy;
import com.example.demo.execution.retry.RetryDecision;
import com.example.demo.execution.retry.RetryPolicy;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class ExponentialBackoffRetryPolicyTest {
	
	private RetryPolicy retryPolicy;
	
	@BeforeEach
	void setup() {
		
		ExponentialBackoffCalculator calculator = new ExponentialBackoffCalculator();
		 
		retryPolicy = new ExponentialBackoffRetryPolicy(calculator);
	}
	
	
	private Job createJob(int maxRetries) {
		
		return Job.create(
				"test-job", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				"http://localhost:9500/test", 
				MisfirePolicy.SKIP_MISSED, 5, maxRetries, 1);
		
	}
	
	
	private JobExecution createExecution(
			UUID jobId,
			int attempt) {
		
		JobExecution execution = 
				JobExecution.create(jobId, Instant.now());
		
		return execution;
	}
	
	private JobExecution startExecution(
			UUID jobId, int attempts) {
		
		JobExecution execution = 
				JobExecution.create(jobId, Instant.now());
		
		for (int i = 0; i < attempts; i++) {
			
			execution.start("worker-1",
					Instant.now().plusSeconds(30));
			
			if (i < attempts -1) {
				execution.fail("test failure");
			}
		}
		
		return execution;
	}
	
	@Test
	void shouldNotRetrySuccessfulExecution() {
		
		Job job = createJob(3);
		
		JobExecution execution = 
				JobExecution.create(job.getId(), Instant.now());
		
		ExecutionResult result = 
				ExecutionResult.succeed();
		
		RetryDecision decision = 
				retryPolicy.decide(job, execution, result);
		
		assertThat(decision.retry()).isFalse();
		
		assertThat(decision.reason()).isEqualTo("Execution Succeeded");
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
