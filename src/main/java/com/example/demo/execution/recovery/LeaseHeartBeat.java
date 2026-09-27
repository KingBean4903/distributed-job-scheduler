package com.example.demo.execution.recovery;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.worker.WorkerIdentity;

@Component
public class LeaseHeartBeat {
	
	private final LeaseRecoveryService leaseService;
	private final JobExectionRepository executionsRepository;
	private WorkerIdentity workerIdentity;
	
	public LeaseHeartBeat(LeaseRecoveryService leaseService,
			JobExectionRepository executionsRepository,
			WorkerIdentity workerIdentity) {
		this.leaseService = leaseService;
		this.workerIdentity = workerIdentity;
		this.executionsRepository = executionsRepository;
	}
	
	@Scheduled(fixedDelay = 10_000)
	public void heartbeat() {
		String workerId = workerIdentity.getWorkerId();
		
		List<JobExecution> executions = 
				executionsRepository.findRunningExections(
						ExecutionStatus.RUNNING, workerId);
		
		for (JobExecution execution: executions) {
			boolean renewed = 
						leaseService.renew(execution.getId(), workerId);
			
			if (!renewed) { 
				
			}
		}
		
	}
	

}
