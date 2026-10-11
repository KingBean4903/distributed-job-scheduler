package com.example.demo.scheduler.execution.lease;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
public class WorkerFencingIntergrationTests {
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private ExecutionClaimService claimService;
	
	@Autowired
	private LeaseRecoveryService recoveryService;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	
	@Test
	@Transactional
	void workerBCanReclaimWorkerAfterWorkerALosesLease() 
			throws InterruptedException {
		
		Job job = createJob();
		
		jobRepository.save(job);
		
		JobExecution execution = 
				JobExecution.create(job.getId(), Instant.now());
		
		executionRepository.save(execution);
		
		UUID executionId = execution.getId();
		
		List<JobExecution> workerAClaims =
				claimService.claim("worker-A", 1);
		
		assertThat(workerAClaims).hasSize(1);
		
		assertThat(workerAClaims.get(0).getId())
				.isEqualTo(executionId);
		
		JobExecution afterWorkerA =
				 executionRepository
				 	.findById(executionId)
				 	.orElseThrow();
		
		Thread.sleep(15000);
		
		int recovered =
				recoveryService.recoverExpiredLeases();
		
		assertThat(recovered).isEqualTo(1);
		
		JobExecution afterRecovery =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(afterRecovery.getStatus())
				.isEqualTo(ExecutionStatus.READY);
		
		assertThat(afterRecovery.getWorkerId())
					.isNull();
		
		assertThat(afterRecovery.getLeaseExpiresAt())
				.isNull();
		
		List<JobExecution> workerBClaims = 
				claimService.claim("worker-B", 1);
		
		assertThat(workerBClaims)
				.hasSize(1);
		
		assertThat(workerBClaims.get(0).getId())
				.isEqualTo(executionId);
		
		JobExecution afterWorkerB = 
					executionRepository
						.findById(executionId)
						.orElseThrow();
		
		assertThat(afterWorkerB.getStatus())
		    .isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(afterWorkerB.getWorkerId())
				.isEqualTo("worker-B");
		
		int staleWorkerResult =
				executionRepository.completeIfOwned(
						executionId, 
						"worker-A", 
						Instant.now());
		
		assertThat(staleWorkerResult)
				.isZero();
		
		JobExecution afterStaleAttempt = 
				executionRepository
					.findById(executionId)
					.orElseThrow();
		
		assertThat(afterStaleAttempt.getStatus())
						.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(afterStaleAttempt.getWorkerId())
				.isEqualTo("worker=B");
		
		int workerBResult = executionRepository
					.completeIfOwned(executionId, "worker-B", 
							Instant.now());
		
		assertThat(workerBResult).isEqualTo(1);
		
		JobExecution finalExecution =
				executionRepository
						.findById(executionId)
						.orElseThrow();
		
		assertThat(finalExecution.getStatus())
				.isEqualTo(ExecutionStatus.SUCCESS);
		
		assertThat(finalExecution.getWorkerId())
				.isEqualTo("worker-B");
	}
	
	private Job createJob() {
		
		return Job.create(
				"fencing-test", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				"http://localhost:8800/test", 
				MisfirePolicy.SKIP_MISSED, 
				1, 
				3, 
				30);
	}
	
}
