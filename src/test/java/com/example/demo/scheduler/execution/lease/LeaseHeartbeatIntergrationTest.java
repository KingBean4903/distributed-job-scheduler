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
import com.example.demo.execution.recovery.LeaseRecoveryService;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest(properties =
"scheduler.worker.lease-duration-seconds:1")
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
		
		Thread.sleep(1500);
		
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
	
	
	@Test
	void heartbeatKeepsLeaseAlive() throws InterruptedException {
		
		Job job = createJob();
		
		jobRepository.save(job);
		
		JobExecution execution =
				JobExecution.create(job.getId(), Instant.now());
		
		executionRepository.save(execution);
		
		List<JobExecution> claimed =
					claimService.claim("worker-A", 1);
		
		UUID executionId =
				claimed.get(0).getId();
		
		JobExecution initial =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		Instant originalExpiry =
				initial.getLeaseExpiresAt();
		
		Thread.sleep(500);
		
		boolean renewed = 
				leaseService.renew(
						executionId, 
						"worker-A");
		
		assertThat(renewed).isTrue();
		
		JobExecution renewedExecution =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(renewedExecution.getLeaseExpiresAt())
				.isAfter(originalExpiry);
		
		Thread.sleep(500);
		
		int recovered = 
			leaseService.recoverExpiredLeases();
		
		assertThat(recovered)
			.isZero();
		
		JobExecution current =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(current.getStatus())
					.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(current.getWorkerId())
					.isEqualTo("worker-A");
		
	}
	
	@Test
	@Transactional
	void expiredLeaseAllowsRecoveryAndFencesStaleWorker() 
					throws InterruptedException {
		
		Job job = createJob();
		
		jobRepository.saveAndFlush(job);
		
		JobExecution execution = JobExecution.create(
				job.getId(),
				Instant.now());
		
		executionRepository.saveAndFlush(execution);
		
		List<JobExecution> workerAClaims = 
				claimService.claim("worker-A1", 1);
		
		UUID executionId = workerAClaims
								.get(0)
								.getId();
		
		Thread.sleep(1_500);
		
		int recovered = 
				leaseService.recoverExpiredLeases();
		
		assertThat(recovered).isEqualTo(1);
		
		
		JobExecution recoveredExecution = 
				executionRepository
					.findById(executionId)
					.orElseThrow();
		
		assertThat(recoveredExecution.getStatus())
			.isEqualTo(ExecutionStatus.READY);
		
		assertThat(recoveredExecution.getWorkerId())
			.isNull();
		
		List<JobExecution> workerBClaims =
				claimService.claim("worker-B1", 1);
		
		assertThat(workerBClaims).hasSize(1);
		
		int staleWorkerResult = 
				executionRepository.completeIfOwned(
						executionId, "worker-A1", Instant.now());
		
		assertThat(staleWorkerResult).isZero();
		
		int workerBResult =
				executionRepository.completeIfOwned(
						executionId, 
						"worker-B1",
						Instant.now());
		
		assertThat(workerBResult)
				.isEqualTo(1);
		
		JobExecution finalExecution =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(finalExecution.getStatus())
				.isEqualTo(ExecutionStatus.SUCCESS);
		
		assertThat(finalExecution.getWorkerId())
				.isEqualTo("worker-B1");
		
		
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
