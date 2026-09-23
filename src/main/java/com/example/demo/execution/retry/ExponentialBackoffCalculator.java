package com.example.demo.execution.retry;

public class ExponentialBackoffCalculator implements 
		BackoffCalculator {
	
	private static final long BASE_DELAY_SECONDS  = 2;
	private static final long MAX_DELAY_SECONDS = 300;

	@Override
	public long calculateDelaySeconds(int attempt) {
	
		long exponential = 
				BASE_DELAY_SECONDS * (1L <<  Math.max(0, attempt - 1));
		
		
		return Math.min(exponential, MAX_DELAY_SECONDS);
	    }
}
