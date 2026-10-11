package com.example.demo.execution.service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.JobExecutorRegistry;
import com.example.demo.execution.repository.JobExectionRepository;

@Service
public class ExecutionCompletionService {

	private final JobExectionRepository repository;
	private final Clock clock;
	
	public ExecutionCompletionService(JobExectionRepository repository,
			Clock clock) {
		
		this.repository = repository;
		this.clock = clock;
	}
	
	@Transactional
	public boolean succeed(UUID executionId, String workerId) {
		
		int updated =
				repository.completeIfOwned(
						executionId, 
						workerId,
						Instant.now(clock));
		
		return updated == 1;
	}
	
	@Transactional
	public void fail(UUID executionId, String error) {
		
		JobExecution execution = 
				repository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found " + executionId));
		
		execution.fail(error);
				
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
