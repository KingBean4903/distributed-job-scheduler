package com.example.demo.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.execution.worker.Worker;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class SchedulerConcurrencyBenchmarkTest {
	
	private static final int JOB_COUNT = 10_000;
	
	private static final int BATCH_SIZE= 10;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	@Autowired
	private ExecutionClaimService claimService;
	
	@Autowired
	Worker worker;
	
	@BeforeEach
	void setup() {
		jobRepository.deleteAll();
		executionRepository.deleteAll();
	}
	
	@Test
	void shouldBenchmarkSingleWorker() {
		
		prepareExecutions();
		
		long start = System.nanoTime();
		
		processJobs(16);
		
		long elapsedNanos = System.nanoTime() - start;
		
		long completed = executionRepository.countByStatus(
				ExecutionStatus.SUCCESS);
		
		double elapsedSeconds = elapsedNanos / 1_000_000_000.00;
		
		double throughput = completed / elapsedSeconds;
		
		System.out.printf("worker :%d execution %d elapsed %.3fs throughput %.2f/sec%n",
				1, completed, elapsedSeconds, throughput);
		
		
		assertThat(completed).isEqualTo(JOB_COUNT);
		
	}
	
	
	private void prepareExecutions() {
		
		
		
		List<Job> jobs = new ArrayList<>();
		
		for (int i = 0; i < JOB_COUNT; i++) {
			Job job =Job.create(
					"benchmark:" + i, 
					JobType.HTTP, 
					"*/5 * * * *", 
					"UTC", 
					"http://localhost:8800/load-test", 
					MisfirePolicy.SKIP_MISSED, 
					1, 
					0, 
					30);
			
			jobs.add(job);
		}
		
		jobRepository.saveAll(jobs);
		jobRepository.flush();
		
		
		List<JobExecution> executions = jobs.stream()
				.map(job -> JobExecution.create(job.getId(), 
						Instant.now())).toList();
		
		executionRepository.saveAll(executions);
		executionRepository.flush();
		
		
		assertThat(jobRepository.count()).isEqualTo(JOB_COUNT);
		
		assertThat(executionRepository.count()).isEqualTo(JOB_COUNT);
		
	}
	
	private void processJobs(int workerCount) {
		
		ExecutorService executor =
				Executors.newFixedThreadPool(workerCount);
		
		try {
			
			while (true) {
				List<JobExecution> executions = 
						claimService.claim("benchmark-worker", BATCH_SIZE);
				
				if (!executions.isEmpty()) {
					for (JobExecution execution: executions) {
						executor.submit(() -> {
							worker.execute(execution);
						});
					}
				}
				
			long ready = executionRepository.countByStatus(
					ExecutionStatus.READY);
			
			long running = executionRepository.countByStatus(
					ExecutionStatus.RUNNING);
			
			if (ready ==0 && running == 0) {
				break;
			}
			
				
			} 
		} finally {
			executor.shutdown();
			try {
				
				boolean finished =
						executor.awaitTermination(10, TimeUnit.MINUTES);
				
				assertThat(finished)
						.as("Workers did not finish within timeout")
						.isTrue();
				
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			
		}
		
	}

}
