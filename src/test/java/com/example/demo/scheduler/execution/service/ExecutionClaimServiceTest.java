package com.example.demo.scheduler.execution.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class ExecutionClaimServiceTest {
	
	@Autowired
	public ExecutionClaimService claimService;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	@Autowired JobRepository jobRepository;
	
	private Job createJob() {
		
		Job job = Job.create(
				"test-job-3", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				null,
				MisfirePolicy.SKIP_MISSED, 5, 3, 30);
		
		job.advanceNextRunAt(Instant.now());
		
		return jobRepository.save(job);
	}
	
	private List<JobExecution> createExecutions(
			Job job, int count) {
		
		List<JobExecution> executions = 
				new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			JobExecution executor = 
					JobExecution.create(
							job.getId(), 
							Instant.now().plusSeconds(i));
			
			executions.add(executionRepository.save(executor));
		}
		
		return executions;
	}
	
	
	@Test
	void shouldClaimReadyExecutions() {
		
		Job job = createJob();
		
		createExecutions(job, 5);
		
		List<JobExecution> claimed = 
				claimService.claim(
						"worker-1", 
						5);
		
		assertThat(claimed).hasSize(5);
		
		assertThat(claimed).allMatch(execution ->
					execution.getStatus() == 
						ExecutionStatus.RUNNING);
		
		assertThat(claimed).allMatch(
				execution -> 
					execution.getWorkerId().equals("worker-1"));
		
		assertThat(claimed).allMatch(
				execution -> execution.getAttempt() == 1);
	
	}
	
	
	@Test
	void shouldNotAllowTwoWorkersToClaimSameExecution() throws Exception { 
		
		Job job = createJob();
		
		createExecutions(job, 5);
		
		ExecutorService executor = 
				Executors.newFixedThreadPool(2);
		
		CountDownLatch start = 
				new CountDownLatch(1);
		
		Future<List<JobExecution>> workerA = 
				executor.submit(() -> {
					start.await();
					return claimService.claim("worker-A-A", 5);
				});
		
		Future<List<JobExecution>> workerB = 
				executor.submit(() -> {
					start.await();
					return claimService.claim("worker-B-B", 5);		
				});
		
		start.countDown();
		
		List<JobExecution> claimedA = 
				 workerA.get();
		
		List<JobExecution> claimedB = 
				workerB.get();
		
		executor.shutdown();
		
		int totalClaimed = 
				claimedA.size() + claimedB.size();
		
		assertThat(totalClaimed).isEqualTo(5);
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


}
