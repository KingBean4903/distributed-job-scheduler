package com.example.demo.execution.service;

import java.util.List;

import com.example.demo.execution.domain.JobExecution;

public interface ExecutionClaimHook {

	void afterExecutionClaimed(
			List<JobExecution> executions);
}
