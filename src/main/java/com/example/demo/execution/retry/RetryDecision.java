package com.example.demo.execution.retry;

public record RetryDecision(
		boolean retry,
		long delaySeconds,
		String reason) {
	
	
	public static RetryDecision retry(
			long delaySeconds,
			String reason) {
		
		return new RetryDecision(
				true, delaySeconds, reason);
	}
	
	public static RetryDecision noRetry(
			String reason) {
		return new RetryDecision(
				false, 0, reason);
	}

}
