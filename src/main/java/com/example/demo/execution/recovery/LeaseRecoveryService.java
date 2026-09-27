package com.example.demo.execution.recovery;

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
	

}
