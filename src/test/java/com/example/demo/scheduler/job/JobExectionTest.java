package com.example.demo.scheduler.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.FailureType;
import com.example.demo.execution.retry.ExponentialBackoffCalculator;
import com.example.demo.execution.retry.ExponentialBackoffRetryPolicy;
import com.example.demo.execution.retry.RetryDecision;
import com.example.demo.execution.retry.RetryPolicy;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;
import com.example.demo.execution.retry.*;


public class JobExectionTest {
	
	
	private ExponentialBackoffRetryPolicy retryPolicy;
	
	@BeforeEach
	void setup() {
		
		ExponentialBackoffCalculator calculator = new ExponentialBackoffCalculator();
		 
		retryPolicy = new ExponentialBackoffRetryPolicy(calculator);
	}
	
	@Test
	void shouldStartReadyExecution() {
		UUID jobId = UUID.randomUUID();
		
		JobExecution execution = 
				JobExecution.create(
						jobId,
						Instant.now());
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThat(execution.getStatus())
				.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(execution.getWorkerId())
				.isEqualTo("worker-1");
		
		assertThat(execution.getAttempt())
				.isEqualTo(1);
		
	}
	
	@Test
	void shouldNotStartExecutionTwice() {
		
		JobExecution execution =
				JobExecution.create(UUID.randomUUID(), Instant.now());
		
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThatThrownBy(() -> 
					execution.start("worker-2",
							Instant.now().plusSeconds(30)))
		.isInstanceOf(IllegalStateException.class);

	}
	
	@Test
	void shouldScheduleFailedExecutionForRetry() {
		
		UUID jobId = UUID.randomUUID();
		
		JobExecution execution =
				JobExecution.create(jobId, 
						Instant.parse("2026-09-12T10:00:00Z"));
		
		execution.start("worker-1", 
				Instant.parse("2026-09-12T10:01:00Z"));
		
		execution.fail("HTTP 500");
		
		Instant retryAt = 
				Instant.parse("2026-09-12T10:02:00Z");
		
		execution.shouldRetry(retryAt);
		
		assertThat(execution.getStatus())
				.isEqualTo(ExecutionStatus.READY);
		
		assertThat(execution.getNextAttemptAt())
				.isEqualTo(retryAt);
		
		assertThat(execution.getLeaseExpiresAt())
				.isNull();
	}
	
	@Test
	void shouldRejectRetryWhenExecutionIsNotFailed() {
		
		JobExecution execution =
				JobExecution.create(UUID.randomUUID(), Instant.now());
		
		assertThatThrownBy(() -> 
					execution.shouldRetry(
							Instant.now().plusSeconds(10)))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Only failed executions can be retried");
		
	}
	
	@Test
	void shouldIncrementAttemptOnlyWhenExecutionStarts() {
		JobExecution execution = 
				JobExecution.create(UUID.randomUUID(), 
						Instant.now());
		
		assertThat(execution.getAttempt())
			.isZero();
		
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThat(execution.getAttempt()).isEqualTo(1);
		
		execution.fail("HTTP 500");
		
		execution.shouldRetry(Instant.now().plusSeconds(10));
		
		assertThat(execution.getAttempt()).isEqualTo(1);
		
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThat(execution.getAttempt()).isEqualTo(2);
		
	}
	
	private Job createJob(int retries) {
		
		return Job.create(
				"generate-report", 
				JobType.HTTP, 
				"0 */5 * * * *", 
				"UTC",
				"{\"customerId\": 123}", 
				MisfirePolicy.SKIP_MISSED,
				5, 
				retries, 
				30);
	}
	
	private JobExecution createFailedExecution(int attempt) {
		
		UUID jobId = UUID.randomUUID();
		
		JobExecution execution =
				JobExecution.create(jobId, 
						Instant.parse("2026-09-12T10:00:00Z"));
		
		execution.start("worker-1", 
				Instant.parse("2026-09-12T10:01:00Z"));
		
		execution.fail("HTTP 500");		
		
		return execution;
	}
	
	@Test
	void shouldRetryAfterFirstAttempt() {
		
		Job job = createJob(3);
		
		JobExecution execution = 
				createFailedExecution(1);
		
		
		ExecutionResult result = ExecutionResult.failure(
				FailureType.SERVER_ERROR,
				500,
				"Internal server error");
		
		RetryDecision decision = 
					retryPolicy.decide(job, execution, result);
		
		
		assertThat(decision.retry()).isTrue();
	}
	
	@Test
	void shouldRetryAfterThirdAttempt() {
		
		Job job = createJob(3);
		
		JobExecution execution = 
				createFailedExecution(1);
		
		ExecutionResult result = ExecutionResult.failure(
		FailureType.SERVER_ERROR,
		500,
		"Internal server error");

		RetryDecision decision = 
					retryPolicy.decide(job, execution, result);
		
		
		assertThat(decision.retry()).isTrue();
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
