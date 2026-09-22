package com.example.demo.execution.retry;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.job.Job;

public interface RetryPolicy {

	RetryDecision decide(
			Job job,
			JobExecution execution,
			ExecutionResult result);
}
