package com.example.demo.execution.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;

import jakarta.transaction.Transactional;

@Service
public class ExecutionClaimService {

	private final JobExectionRepository executionRepository;
	private final ExecutionClaimHook claimHook;
	
	public ExecutionClaimService(
			JobExectionRepository executionRepository,
			ExecutionClaimHook claimHook) {
		this.executionRepository = executionRepository;
		this.claimHook = claimHook;
	}
	
	@Transactional
	public List<JobExecution> claim(
			String workerId,
			int limit) {
		
		List<JobExecution> executions =
				executionRepository.findReadyExecutionsForUpdate(limit);
		
		claimHook.afterExecutionClaimed(executions);
		
		Instant leaseExpiresAt =
				Instant.now().plusSeconds(30);
		
		for (JobExecution execution: executions) {
			execution.start(workerId, leaseExpiresAt);
		}
		
		return executions;
	}
	
}
