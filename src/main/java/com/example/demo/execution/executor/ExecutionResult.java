package com.example.demo.execution.executor;

public record ExecutionResult(
		boolean success,
		FailureType failureType,
		Integer httpStatus,
		String error) {
	
	public static ExecutionResult succeed() {
		return new ExecutionResult(true, null, null, null);
	}
	
	public static ExecutionResult failure(
			FailureType failureType,
			Integer httpStatus,
			String error) {
		
		return new ExecutionResult(
				false, 
				failureType,
				httpStatus,
				error);
	}
}
