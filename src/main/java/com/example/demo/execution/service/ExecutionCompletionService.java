package com.example.demo.execution.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.JobExecutorRegistry;
import com.example.demo.execution.repository.JobExectionRepository;

@Service
public class ExecutionCompletionService {

	private final JobExectionRepository repository;
	
	public ExecutionCompletionService(JobExectionRepository repository) {
		
		this.repository = repository;
	}
	
	@Transactional
	public void succeed(UUID executionId) {
		
		JobExecution execution = 
				repository.findById(executionId)
						.orElseThrow(() -> 
						 new IllegalStateException("Execution not found" + executionId));
		
		execution.succeed();
	}
	
	@Transactional
	public void fail(UUID executionId, String error) {
		
		JobExecution execution = 
				repository.findById(executionId)
				.orElseThrow(() -> new IllegalStateException("Execution not found " + executionId));
		
		execution.fail(error);
				
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
