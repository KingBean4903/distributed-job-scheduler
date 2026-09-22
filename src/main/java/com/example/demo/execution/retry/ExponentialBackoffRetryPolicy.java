package com.example.demo.execution.retry;

import java.util.concurrent.ThreadLocalRandom;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.FailureType;
import com.example.demo.job.Job;

public class ExponentialBackoffRetryPolicy
 		implements RetryPolicy {

	private static final long BASE_DELAY_SECONDS = 1;
	private static final long MAX_DELAY_SECONDS = 300;
	
	@Override
	public RetryDecision decide(Job job, JobExecution execution, ExecutionResult result) {
		
		if (result.success()) {
			return  RetryDecision.noRetry("Execution Succeeded");
		}
		
		if (!isRetrayable(result)) {
			return RetryDecision.noRetry("Failure is not negligible");
		}
		
		if (execution.getAttempt() >= job.getMaxRetries()) {
			return RetryDecision.noRetry("Maximum retries exceeded");
		}
		
		
		long delay = calculateDelay(execution.getAttempt());
		
		return RetryDecision.retry(
				delay,
				"Retryable failure");
	}
	
	
	
	private boolean isRetrayable(ExecutionResult result) {
		
		
		if (result.failureType() == null) {
			return false;
		}
		
		return switch(result.failureType()) { 
		case SERVER_ERROR,
			TIMEOUT,
			CONNECTION_ERROR -> true;
		case CLIENT_ERROR ->
				result.httpStatus() != null &&
				(result.httpStatus() == 400 || result.httpStatus() == 429);
		case UNKNOWN -> false;
		};
	}
	
	private long calculateDelay(int attempt) {
		long exponential = 
				BASE_DELAY_SECONDS * (1L << Math.max(0, attempt - 1));
		
		long capped = Math.min(exponential, MAX_DELAY_SECONDS);
		
		return addJitter(capped);
	}
	
	private long addJitter(long delay) {
		
		long jitter = 
				 ThreadLocalRandom.current()
				 .nextLong(0,
						 Math.max(1, delay/2));
		
		return delay + jitter;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
