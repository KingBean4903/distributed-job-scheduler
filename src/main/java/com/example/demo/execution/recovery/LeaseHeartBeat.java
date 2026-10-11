package com.example.demo.execution.recovery;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.worker.ActiveExecutionRegistry;
import com.example.demo.execution.worker.WorkerIdentity;

@Component
public class LeaseHeartBeat {
	
	private final ActiveExecutionRegistry activeExecutionRegistry;
	private final LeaseRecoveryService leaseService;
	private final JobExectionRepository executionsRepository;
	private WorkerIdentity workerIdentity;
	
	public LeaseHeartBeat(LeaseRecoveryService leaseService,
			JobExectionRepository executionsRepository,
			WorkerIdentity workerIdentity,
			ActiveExecutionRegistry   activeExecutionRegistry) {
		this.leaseService = leaseService;
		this.workerIdentity = workerIdentity;
		this.executionsRepository = executionsRepository;
		this.activeExecutionRegistry = activeExecutionRegistry;
	}
	
	@Scheduled(fixedDelayString = "${scheduler.worker.heartbeat-interval-millis:10000}")
	public void heartbeat() {
		//String workerId = workerIdentity.getWorkerId();
		
		List<JobExecution> executions = 
				executionsRepository.findRunningExections(
						ExecutionStatus.RUNNING, "benchmark-worker");
		
		for (UUID ID: activeExecutionRegistry.snapshot()) {
			boolean renewed = 
						leaseService.renew(ID, "benchmark-worker");
			
			if (!renewed) { 
				
			}
		}
		
	}
	

}
