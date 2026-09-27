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
public class LeaseRecoveryIntergrationTest {
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobExectionRepository executionRepository;
	
	@Autowired
	ExecutionClaimService claimService;
	
	@Autowired
	LeaseRecoveryService leaseService;
	
	@BeforeEach
	void clearDatabase() {
		executionRepository.deleteAll();
		jobRepository.deleteAll();
	}
	
	@Test
	void expiredWorkerLosesOwnership() throws Exception {
		
		Job job = Job.create(
				"Test Print JOb", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", 
				"https://example.com", 
				MisfirePolicy.SKIP_MISSED, 
				1, 3, 30);
		
		jobRepository.save(job);
		
		JobExecution execution =
					JobExecution.create(job.getId(), Instant.now());
		
		executionRepository.save(execution);
		
		// Worker A claims it
		List<JobExecution> workerAClaims = 
					claimService.claim("worker-A", 1);
		
		assertThat(workerAClaims).hasSize(1);
		
		UUID executionId = workerAClaims.get(0).getId();
		
		JobExecution running = 
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(running.getWorkerId()).isEqualTo("worker-A");
		
		Thread.sleep(1500);
		
		// Recovery detects abandoned execution
		
		int recovered = 
				leaseService.recoverExpiredLeases();
		
		assertThat(recovered).isEqualTo(1);
		
		//6. Execution is ready again
		JobExecution ready =
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(ready.getStatus()).isEqualTo(ExecutionStatus.READY);
		assertThat(ready.getLeaseExpiresAt()).isNull();
		
		// 7 Worker B claims it
		List<JobExecution> workerBClaims = 
					claimService.claim("worker-B", 1);
		
		assertThat(workerBClaims).hasSize(1);
		
		//8. Worker aA tries to complete
		int staleWorkerUpdate = executionRepository.completeIfOwned(executionId, "worker-A");
		assertThat(staleWorkerUpdate).isZero();
		
		//9. WOrker B completes successfully
		int validWorkerUpdate = 
				executionRepository.completeIfOwned(executionId, "worker-B");
		assertThat(validWorkerUpdate).isEqualTo(1);
		
		// 10. Final state
		JobExecution completed = 
				executionRepository.findById(executionId)
				.orElseThrow();
		
		assertThat(completed.getStatus()).isEqualTo(ExecutionStatus.SUCCESS);
		assertThat(completed.getWorkerId()).isEqualTo("worker-B");
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
