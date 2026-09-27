package com.example.demo.scheduler.execution.lease;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.recovery.LeaseRecoveryService;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class LeaseHeartbeatIntergrationTest {
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobExectionRepository executionRepository;
	
	@Autowired
	ExecutionClaimService claimService;

	@Autowired
	LeaseRecoveryService leaseService;
	
	@BeforeEach
	void cleanDatastore() {
		executionRepository.deleteAll();
		jobRepository.deleteAll();
	}
	
	
	@Test
	void heartbeatKeepLeaseAlive() throws InterruptedException {
		
		Job job = createJob();
		
		jobRepository.save(job);
		
		JobExecution execution = JobExecution.create(
				job.getId(), Instant.now());
		
		executionRepository.save(execution);
		
		List<JobExecution> claimed = 
				claimService.claim("worker-A", 1);
		
		assertThat(claimed).hasSize(1);
		
		UUID executionId = 
				claimed.get(0).getId();
		
		JobExecution initial = 
				executionRepository.findById(executionId)
				.orElseThrow();
		
		Instant originalLease = initial.getLeaseExpiresAt();
		
		assertThat(originalLease).isAfter(Instant.now());
		
		Thread.sleep(500);
		
		boolean renewed =
				leaseService.renew(executionId, "worker-A");
		
		assertThat(renewed).isTrue();
		
		JobExecution afterHeartBeat =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(afterHeartBeat.getLeaseExpiresAt())
				.isAfter(originalLease);
		
		
		Thread.sleep(700);
		
		
		JobExecution stillOwned = 
					executionRepository.findById(executionId)
					.orElseThrow();
		
		assertThat(stillOwned.getStatus())
				.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(stillOwned.getWorkerId())
			.isEqualTo("worker-A");
		
		assertThat(stillOwned.getLeaseExpiresAt())
			.isAfter(Instant.now());
		
	}
	
	
	private Job createJob() {
		return Job.create(
				"PAYMENT COMPLETE TEST", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				"http://example.com", 
				MisfirePolicy.SKIP_MISSED, 
				1, 
				3, 
				30);
		
				
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
