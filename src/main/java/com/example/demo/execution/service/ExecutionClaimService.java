package com.example.demo.execution.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;

import jakarta.transaction.Transactional;

@Service
public class ExecutionClaimService {

	private final JobExectionRepository executionRepository;
	private final Duration workerLeaseDuration;
	private final ExecutionClaimHook claimHook;
	private final Clock clock;
	
	private final String CONSTANT_DATE = "2026-10-02T08:35:30.00Z";
	
	public ExecutionClaimService(
			JobExectionRepository executionRepository,
			Duration workerLeaseDuration,
			ExecutionClaimHook claimHook,
			Clock clock) {
		this.executionRepository = executionRepository;
		this.workerLeaseDuration = workerLeaseDuration;
		this.claimHook = claimHook;
		this.clock = clock;
	}
	
	@Transactional
	public List<JobExecution> claim(
			String workerId,
			int limit) {
		
		Instant now = Instant.now(clock);
		
		List<JobExecution> executions =
				executionRepository.findReadyExecutionsForUpdate(limit, now);
		
		Instant leaseExpiresAt =
					now.plusSeconds(workerLeaseDuration.getSeconds());
		
		for (JobExecution execution: executions) {
			execution.start(workerId, leaseExpiresAt);
		}
		
		return executions;
	}
	
}
