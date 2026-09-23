package com.example.demo.scheduler.execution.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.FailureType;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.retry.ExponentialBackoffCalculator;
import com.example.demo.execution.retry.ExponentialBackoffRetryPolicy;
import com.example.demo.execution.retry.RetryService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobStatus;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class RetryServiceIntergrationTest {
	
	@Autowired
	private RetryService retryService;
	
	@Autowired
	private JobRepository repository;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	
	
	
	Job createAndSaveJob(int maxRetries) {
		
		Job job = Job.create(
				"send-email", 
				JobType.HTTP, 
				"0 */5 * * * *", 
				"UTC",
				"{\"customerId\": 123}", 
				MisfirePolicy.SKIP_MISSED,
				5, 
				maxRetries, 
				30);
		
		return repository.save(job);
		
	}
	
	@Test
	void shouldScheduleRetryAfterServerError() {
		
		Job job = createAndSaveJob(3);
		
		JobExecution execution = 
				JobExecution.create(job.getId(),Instant.now());
		
		execution.start("worker-33", Instant.now().plusSeconds(30));
		
		executionRepository.save(execution);
		
		
		ExecutionResult result = 
				ExecutionResult.failure(
						FailureType.SERVER_ERROR, 
						500, 
						"Internal server error");
		
		
		Instant before = Instant.now();
		
		retryService.handleFailure(job, execution.getId(), result);
		
		Instant after = Instant.now();
		
		JobExecution updated = 
				executionRepository.findById(execution.getId())
				.orElseThrow();
		
		assertThat(updated.getStatus()).isEqualTo(ExecutionStatus.READY);
		
		assertThat(updated.getAttempt()).isEqualTo(1);
		
		assertThat(updated.getNextAttemptAt()).isAfter(before);
		
		assertThat(updated.getNextAttemptAt()).isBefore(after.plusSeconds(5));
	}
	
	
	@Test
	void shouldMoveExecutionToDlqWhenRetriesExhausted() {
		
		Job job = createAndSaveJob(3);
		
		JobExecution execution = JobExecution.create(
				job.getId(), 
				Instant.now());
		
		// Attempt 1
		execution.start("worker-1", Instant.now().plusSeconds(30));
		execution.fail("HTTP 500");
		
		// Retry - READY
		execution.scheduleRetry(Instant.now());
		
		// Attempt 2
		execution.start("worker-1", Instant.now().plusSeconds(30));
		execution.fail("HTTP 500");
		
		execution.scheduleRetry(Instant.now());
		
		// Attempt 3
		execution.start("worker-1", Instant.now().plusSeconds(30));
		execution.fail("HTTP 500");
		
		execution.scheduleRetry(Instant.now());
		
		// Attempt 4
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		executionRepository.save(execution);
				
		ExecutionResult result =
				ExecutionResult.failure(
						FailureType.SERVER_ERROR, 
						500, 
						"Internal server error");
		
		retryService.handleFailure(job, execution.getId(), result);
				
		
		JobExecution updated = 
				executionRepository.findById(execution.getId())
						.orElseThrow();
		
		assertThat(updated.getStatus())
					.isEqualTo(ExecutionStatus.DLQ);
		
		assertThat(updated.getAttempt())
				.isEqualTo(4);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
