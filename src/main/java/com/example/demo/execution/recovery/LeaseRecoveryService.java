package com.example.demo.execution.recovery;


import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.repository.JobExectionRepository;

@Service
public class LeaseRecoveryService {
	
	private final JobExectionRepository 
					executionRepository;
	private final Duration workerLeaseDuration;
	private final Clock clock;
	
	public LeaseRecoveryService(
			JobExectionRepository executionRepository,
			@Qualifier("workerLeaseDuration")
			Duration workerLeaseDuration,
			Clock clock) {
		this.executionRepository = executionRepository;
		this.workerLeaseDuration = workerLeaseDuration;
		this.clock = clock;
	}
	
	@Transactional
	public int recoverExpiredLeases() {
		
		return executionRepository
					.recoverExpiredLeases(Instant.now(clock));
	}
	
	@Transactional
	public boolean renew(UUID executionId, 
			String workerId) {
		
	
		
		Instant now = Instant.now(clock);
		Instant newLeaseExpiresAt = now.plus(workerLeaseDuration);
		
		return executionRepository.renewLease(
				executionId, 
				workerId, 
				newLeaseExpiresAt, 
				now) == 1;
	}
	

}
