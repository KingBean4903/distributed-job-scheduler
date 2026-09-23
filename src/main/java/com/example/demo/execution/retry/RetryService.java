package com.example.demo.execution.retry;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.job.Job;

@Service
public class RetryService {
	
	private final JobExectionRepository executionRepository;
	private final RetryPolicy retryPolicy;
	
	public RetryService(
			JobExectionRepository executionRepository,
			RetryPolicy retryPolicy) {
		this.executionRepository = executionRepository;
		this.retryPolicy = retryPolicy;
	}
	
	@Transactional
	public void handleFailure(
			Job job,
			JobExecution jobExecution,
			ExecutionResult result) { 
		
		RetryDecision decision = 
					retryPolicy.decide(job, jobExecution, result);
		
		if (decision.retry()) {
			Instant nextAttemptAt = Instant
					.now()
					.plusSeconds(decision.delaySeconds());
			jobExecution.shouldRetry(nextAttemptAt);
		} else {
			jobExecution.moveToDlq(decision.reason());
		}
	}
	

}
