package com.example.demo.scheduler.job.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.JobSchedulingService;
import com.example.demo.scheduling.scheduler.MisfirePolicy;
import com.example.demo.scheduling.scheduler.SchedulingHook;

@SpringBootTest
public class JobSchedulingConcurrencyTest {

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	@Autowired
	private JobSchedulingService schedulingService;
	
	@BeforeEach
	void cleanDatabase() {
		executionRepository.deleteAll();
		jobRepository.deleteAll();
	}
	
	@TestConfiguration
	static class TestConfig {
		
		@Bean
		SchedulingHook schedulingHook() {
			return new BlockingSchedulingHook();
		}
	}
	
	static class BlockingSchedulingHook implements SchedulingHook {
		
		private final CountDownLatch jobLocked = 
				new CountDownLatch(1);
		
		private final CountDownLatch continueTrxn = 
				new CountDownLatch(1);
		
		
		@Override
		public void afterJobsLocked(List<Job> jobs) {
			
			if (jobs.isEmpty()) {
				jobLocked.countDown();
				
				try {
					continueTrxn.await();
				} catch(InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException();
				}
			}
			
		}
		
		public void release() {
			continueTrxn.countDown();
		}
		
		public boolean awaitJobLocked() throws InterruptedException {
			return jobLocked.await(5, TimeUnit.SECONDS);
		}
		
		
	}
	
	@Test
	void shouldCreateOnlyOneExecutionWhenTwoSchedulersRunConcurrently() throws Exception {
		
		Job job = Job.create(
				"test-job",
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC",
				"{}", 
				MisfirePolicy.SKIP_MISSED,
				1, 
				3, 
				10);
		
		job.advanceNextRunAt(Instant.now().minusSeconds(10));
		
		jobRepository.saveAndFlush(job);
		
		ExecutorService executor =
				Executors.newFixedThreadPool(2);
		
		CountDownLatch start = 
				new CountDownLatch(1);
				
		Callable<Void> schedulerTask = () -> {
			start.await();
			schedulingService.processDueJobs();
			return null;
		};
		
		Future<Void> schedulerA = 
					executor.submit(schedulerTask);
		
		Future<Void> schedulerB = 
				executor.submit(schedulerTask);
		
		start.countDown();
		
		schedulerA.get();
		schedulerB.get();
		
		executor.shutdown();
		
		List<JobExecution> executions = 
				executionRepository.findAll();
		
		assertThat(executions).hasSize(1);
		
	}
	
	@Test
	void shouldRejectDuplicateExecutionForSameSchedulerOccurrence() {
		
		Job job = Job.create(
				"duplicate-test", 
				JobType.HTTP, 
				"*/5 * * * * *", 
				"UTC",
				"{}", 
				MisfirePolicy.SKIP_MISSED,
				1, 
				3, 
				30);
		
		job.advanceNextRunAt(
				Instant.now().plusSeconds(10));
		
		jobRepository.saveAndFlush(job);
		
		Instant scheduledAt = job.getNextRunAt();
		
		JobExecution first = JobExecution.create(job.getId(), scheduledAt);
		
		JobExecution second = JobExecution.create(job.getId(), scheduledAt);

		
		executionRepository.saveAndFlush(first);
		
		assertThatThrownBy(() -> 
					executionRepository.saveAndFlush(second))
		.isInstanceOf(DataIntegrityViolationException.class);
		
	} 
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
