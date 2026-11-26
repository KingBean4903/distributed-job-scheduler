package com.example.demo.execution.worker;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(Worker.class);
	
	private final ExecutionClaimService claimService;
	private final JobRepository jobRepository;
	private final JobExecutorRegistry executorRegistry;
	private final ExecutionCompletionService completionService;
	private final ExecutorService workerExecutor;
	private final RetryService retryService;
	private final WorkerIdentity workerIdentity;
	private final ActiveExecutionRegistry activeExecutionsRegistry;
	
	private final Map<Double, String> lookups  = new TreeMap<>();
	private final Map<Double, String> work  = new TreeMap<>();
	private final Map<Double, String> completion  = new TreeMap<>();
	private final Map<Double, String> totals  = new TreeMap<>();
	
	private List<Double> claims = new ArrayList<>();
	
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
		
		long start = System.nanoTime();
		
		List<JobExecution> executions = 
				claimService.claim(workerId, batchSize);
		
		double duration = (System.nanoTime() - start) / 1_000_000_000.00;
		
		claims.add(duration);
		
		for (JobExecution execution: executions) {
			
			activeExecutionsRegistry.add(execution.getId());
			
		    workerExecutor.submit(() -> execute(execution));
		}
		
	}
	
	public List<Double> getClaims() {
		return claims;
	}
	
	public void execute(JobExecution execution, 
			ConcurrentHashMap<String, Map<String, Double>> stats,
			
			LatencyRecorderInterface recorder,
			LatencyRecorderInterface lookupRcd) {
		
		long totalStart = System.nanoTime();
		
		
		try {
			
			long jobLookupStart = System.nanoTime();
			
			Job job = jobRepository.findById(execution.getJobId())
							.orElseThrow(() -> 
							new IllegalStateException(
									"Job not found " + 
									execution.getJobId()));
			
			long lookupNanos = System.nanoTime() - jobLookupStart;
			
			JobExecutor executor = executorRegistry
					.getExecutor(job.getJobType());
			
			long workStart = System.nanoTime();
			
			ExecutionResult result = executor.execute(job, execution);
			
			long workNanos = System.nanoTime() - workStart;
			
			activeExecutionsRegistry.add(execution.getId());
			
			long completionStart = System.nanoTime();
			
			String workerId = workerIdentity.getWorkerId();
			
			if (result.success()) {
				
				boolean completed =
									completionService.succeed(
											execution.getId(), 
											workerId);
				
				if (!completed) {
					// We lost the ownership
					// Do not mutate the execution
					throw new IllegalStateException("Lost ownership of execution "  + execution.getId());
				}
				
						
			} else {
			//	retryService.handleFailure(job, execution.getId()kkk, result);
				completionService.fail(execution.getId(), result.error());
			}	
			
			long completionNanos = System.nanoTime() - completionStart;
			
			recorder.record(completionNanos);
			lookupRcd.record(lookupNanos);
			
//			log.debug(
//					"executionId={} lookupMs={} workMs={} completionMs={} totalMs={}",
//					execution.getId(), 
//					lookupNanos / 1_000_000.00,
//					workNanos / 1_000_000.00,
//					completionNanos / 1_000_000.00,
//					System.nanoTime() - totalStart / 1_000_000.00);
			
//			lookups.put(lookupNanos / 1_000_000.00, execution.getId().toString());
//			work.put(workNanos / 1_000_000.00, execution.getId().toString());
//			completion.put(completionNanos / 1_000_000.00, execution.getId().toString());
//			totals.put(System.nanoTime() - totalStart / 1_000_000.00, execution.getId().toString());
			
			stats.put(execution.getId().toString(), 
					Map.of("lookup", lookupNanos/ 1_000_000.00,
							"work", workNanos / 1_000_000.00,
							"completion", completionNanos / 1_000_000.00,
							"total", (System.nanoTime() - totalStart) / 1_000_000.00));
				
			
			
		}  finally {
						
			activeExecutionsRegistry.remove(execution.getId());
		}
	} 
	
	
	
public void execute(JobExecution execution, AtomicInteger counter) {
		
		long totalStart = System.nanoTime();
		
		try {
			
			long jobLookupStart = System.nanoTime();
			
			
			counter.incrementAndGet();
			
			
			Job job = jobRepository.findById(execution.getJobId())
							.orElseThrow(() -> 
							new IllegalStateException(
									"Job not found " + 
									execution.getJobId()));
			
			long lookupNanos = System.nanoTime() - jobLookupStart;
			
			JobExecutor executor = executorRegistry
					.getExecutor(job.getJobType());
			
			long workStart = System.nanoTime();
			
			ExecutionResult result = executor.execute(job, execution);
			
			long workNanos = System.nanoTime() - workStart;
			
			
			activeExecutionsRegistry.add(execution.getId());
			
			long completionStart = System.nanoTime();
			
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
			
			long completionNanos = System.nanoTime() - completionStart;
			
//			log.debug(
//					"executionId={} lookupMs={} workMs={} completionMs={} totalMs={}",
//					execution.getId(), 
//					lookupNanos / 1_000_000.00,
//					workNanos / 1_000_000.00,
//					completionNanos / 1_000_000.00,
//					System.nanoTime() - totalStart / 1_000_000.00);
			
			lookups.put(lookupNanos / 1_000_000.00, execution.getId().toString());
			work.put(workNanos / 1_000_000.00, execution.getId().toString());
			completion.put(completionNanos / 1_000_000.00, execution.getId().toString());
			totals.put(System.nanoTime() - totalStart / 1_000_000.00, execution.getId().toString());
			
			log.debug("counter={}", counter.get());
			
			if (counter.get() == 10000) {
				
					double mLookups = medianCalc(lookups);
					double mWork = medianCalc(work);
					double mCompletion = medianCalc(completion);
					double mTotal = medianCalc(totals);
					
					
					log.debug(
							"executionId={} lookupMs={} workMs={} completionMs={} totalMs={}",
							execution.getId(), 
							mLookups,
							mWork,
							mCompletion,
							mTotal);
				
			}
			
		}  finally {
						
			activeExecutionsRegistry.remove(execution.getId());
		}
	} 

public void execute(JobExecution execution) {
	
	long totalStart = System.nanoTime();
	
	try {
		
		long jobLookupStart = System.nanoTime();
		
		
		
		
		Job job = jobRepository.findById(execution.getJobId())
						.orElseThrow(() -> 
						new IllegalStateException(
								"Job not found " + 
								execution.getJobId()));
		
		long lookupNanos = System.nanoTime() - jobLookupStart;
		
		JobExecutor executor = executorRegistry
				.getExecutor(job.getJobType());
		
		
		ExecutionResult result = executor.execute(job, execution);
		
		
		
		activeExecutionsRegistry.add(execution.getId());
		
		long completionStart = System.nanoTime();
		
		String workerId = workerIdentity.getWorkerId();
		
		if (result.success()) {
			
			boolean completed =
								completionService.succeed(
										execution.getId(), 
										workerId);
			
			if (!completed) {
				// We lost the ownership
				// Do not mutate the execution
				throw new IllegalStateException("Lost ownership of execution "  + execution.getId());
			}
			
					
		} else {
		//	retryService.handleFailure(job, execution.getId()kkk, result);
			completionService.fail(execution.getId(), result.error());
		}	
		
		long completionNanos = System.nanoTime() - completionStart;
		

		
	}  finally {
					
		activeExecutionsRegistry.remove(execution.getId());
	}
} 
	
	
	public Set<UUID> getActiveExecutions() {
		return activeExecutionsRegistry.snapshot();
	}
	
	public Double medianCalc(Map<Double, String> items) {
		
		List<Double> lList = items.keySet().stream().collect(Collectors.toList());
		
		int midIdx =  (lList.size()/2) - 1;
		
		double midV = lList.get(midIdx);
		double midV2 = lList.get(midIdx + 1);
		
		double median = (midV + midV2) / 2;
		
		return median;
	}
	

}
