package com.example.demo.execution.worker;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.JobExecutor;
import com.example.demo.execution.executor.JobExecutorRegistry;
import com.example.demo.execution.retry.RetryService;
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
	private final ExecutorService workerExecutor;
	private final RetryService retryService;
	private final WorkerIdentity workerIdentity;
	private final ActiveExecutionRegistry activeExecutionsRegistry;
	

	
	public Worker(
			ExecutionClaimService claimService,
			JobRepository jobRepository,
			JobExecutorRegistry executorRegistry,
			ExecutionCompletionService completionService,
			ExecutorService workerExecutor,
			RetryService retryService,
			WorkerIdentity workerIdentity,
			ActiveExecutionRegistry activeExecutionsRegistry) {
		this.claimService = claimService;
		this.jobRepository = jobRepository;
		this.completionService = completionService;
		this.workerExecutor = workerExecutor;
		this.executorRegistry = executorRegistry;
		this.retryService = retryService;
		this.workerIdentity = workerIdentity;
		this.activeExecutionsRegistry = activeExecutionsRegistry;
	}
	
	public void runOnce( 
			int batchSize) {
		
		String workerId = workerIdentity.getWorkerId();
		
		
		List<JobExecution> executors = 
				claimService.claim(workerId, batchSize);
		
		for (JobExecution executor: executors) {
			activeExecutionsRegistry.add(executor.getId());
			workerExecutor.submit(() ->
			execute(executor));
		}
		
	}
	
	public void execute(JobExecution execution) {
		
		try {
			
			Job job = jobRepository.findById(execution.getJobId())
							.orElseThrow(() -> 
							new IllegalStateException(
									"Job not found " + 
									execution.getJobId()));
			
			JobExecutor executor = executorRegistry
					.getExecutor(job.getJobType());
			
			ExecutionResult result = executor.execute(job, execution);
			activeExecutionsRegistry.add(execution.getId());
			
			//String workerId = workerIdentity.getWorkerId();
			
			if (result.success()) {
				
				boolean completed =
									completionService.succeed(
											execution.getId(), 
											"benchmark-worker");
				
				if (!completed) {
					// We lost the ownership
					// Do not mutate the execution
					throw new IllegalStateException("Lost ownership of execution "  + execution.getId());
				}
				
						
			} else {
			//	retryService.handleFailure(job, execution.getId()kkk, result);
				completionService.fail(execution.getId(), result.error());
			}		
		}  finally {
						
			activeExecutionsRegistry.remove(execution.getId());
		}
	} 
	
	
	public Set<UUID> getActiveExecutions() {
		return activeExecutionsRegistry.snapshot();
	}
	

}
