package com.example.demo.execution.retry;

public interface BackoffCalculator {
	long calculateDelaySeconds(int attempt);
}
