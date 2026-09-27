package com.example.demo.execution.recovery;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.repository.JobExectionRepository;

@Service
public class LeaseRecoveryService {
	
	private final JobExectionRepository 
					executionRepository;
	
	public LeaseRecoveryService(
			JobExectionRepository executionRepository) {
		this.executionRepository = executionRepository;
	}
	
	@Transactional
	public int recoverExpiredLeases() {
		return executionRepository
					.recoverExpiredLeases();
	}
	
	@Transactional
	public boolean renew(UUID executionId, 
			String workerId) {
		
		return executionRepository.renewLease(executionId, workerId) == 1;
	}
	

}
