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
	private final ExponentialBackoffCalculator backoffCalculator;
	
	public ExponentialBackoffRetryPolicy(
			ExponentialBackoffCalculator backoffCalculator) {
		this.backoffCalculator = backoffCalculator;
	}
	
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
		
		
		long delay = backoffCalculator.calculateDelaySeconds(execution.getAttempt());
		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
