package com.example.demo.execution.worker;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.JobExecutor;
import com.example.demo.execution.executor.JobExecutorRegistry;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.execution.service.ExecutionCompletionService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;

@Component
public class Worker {
	
	private final ExecutionClaimService claimService;
	private final JobRepository jobRepository;
	private final JobExecutorRegistry executorRegistry;
	private final ExecutionCompletionService completionService;
	
	public Worker(
			ExecutionClaimService claimService,
			JobRepository jobRepository,
			JobExecutorRegistry executorRegistry,
			ExecutionCompletionService completionService) {
		this.claimService = claimService;
		this.jobRepository = jobRepository;
		this.completionService = completionService;
		this.executorRegistry = executorRegistry;
	}
	
	public void runOnce(String workerId, 
			int batchSize) {
		
		List<JobExecution> executors = 
				claimService.claim(workerId, batchSize);
		
		for (JobExecution executor: executors) {
			execute(executor);
		}
		
	}
	
	private void execute(JobExecution execution) {
		
		Job job = jobRepository.findById(execution.getJobId())
						.orElseThrow(() -> 
						new IllegalStateException(
								"Job not found " + 
								execution.getJobId()));
		
		JobExecutor executor = executorRegistry
				.getExecutor(job.getJobType());
		
		ExecutionResult result = executor.execute(job, execution);
		
		if (result.success()) {
			completionService.succeed(execution.getId());
		} else {
			completionService.fail(execution.getId(), result.error());
		}		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
