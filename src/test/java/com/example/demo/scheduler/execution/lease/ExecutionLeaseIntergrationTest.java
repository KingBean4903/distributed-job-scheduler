package com.example.demo.scheduler.execution.lease;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class ExecutionLeaseIntergrationTest {
	
	@Autowired
	JobExectionRepository executionRepository;
	
	
	@Autowired
	ExecutionClaimService claimService;
	
	@Autowired
	JobRepository jobRepository;
	
	@BeforeEach
	void setup() {
		executionRepository.deleteAllInBatch();
		jobRepository.deleteAllInBatch();
		
		assertThat(executionRepository.count()).isZero();
		assertThat(jobRepository.count()).isZero();
	}
	
	
	@Test
	@Transactional
	void ownerCanCompleteExecution() {
		
		Job job = createJob();
		
		jobRepository.save(job);
		
		JobExecution execution =
				JobExecution.create(job.getId(),
						Instant.now());
		
		
		JobExecution saved  = executionRepository.save(execution);
		assertThat(executionRepository.count()).isEqualTo(1);
		
		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getStatus()).isEqualTo(ExecutionStatus.READY);
		assertThat(saved.getNextAttemptAt()).isBefore(Instant.now());
				
		List<JobExecution> claimed = 
				claimService.claim("worker-A", 1);
		
		assertThat(claimed).hasSize(1);
		
				
		UUID executionId = claimed.get(0).getId();
		
		assertThat(executionId).isNotNull();
		
		int updated = executionRepository.completeIfOwned(executionId, "worker-A");
		
		assertThat(updated).isEqualTo(1);
		
		JobExecution completed = 
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(completed.getStatus())
				.isEqualTo(ExecutionStatus.SUCCESS);
	}
	
	@Test
	@Transactional
	void staleWorkerCannotCompleteExecution() {
		
		Job job = createJob();
		
		jobRepository.save(job);
		
		JobExecution execution = 
				JobExecution.create(job.getId(), Instant.now());
		
		executionRepository.save(execution);
		
		List<JobExecution> claimed =
				claimService.claim("worker-A", 1);
		
		UUID executionId =
					claimed.get(0).getId();
		
		
		// Simulator WorkerB taking ownership
		int reassigned =
				executionRepository.reassignForTest(
						executionId, 
						"worker-2");
		
		assertThat(reassigned).isEqualTo(reassigned);
		
		int completed =
				executionRepository.completeIfOwned(
						executionId, 
						"worker-2");
		
		assertThat(completed).isEqualTo(0);
		
		JobExecution current =
				executionRepository
					.findById(executionId)
					.orElseThrow();
		
		assertThat(current.getStatus())
				.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(current.getWorkerId())
				.isEqualTo("worker-2");
		
	}
	
	
	
	private Job createJob() {
		
		return Job.create(
				"Email Job", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				"https://example.com", MisfirePolicy.SKIP_MISSED, 1, 3, 30);
	}
	
	

}
