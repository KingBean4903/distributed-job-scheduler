package com.example.demo.execution.executor;

public record ExecutionResult(
		boolean success,
		String error) {
	
	public static ExecutionResult succeed() {
		return new ExecutionResult(true, null);
	}
	
	public static ExecutionResult failure(String error) {
		return new ExecutionResult(false, error);
	}
}
