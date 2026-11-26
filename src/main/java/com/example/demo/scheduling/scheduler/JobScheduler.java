package com.example.demo.scheduling.scheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.execution.worker.Worker;

@Component
public class JobScheduler {
	
	private final JobSchedulingService jobSchedulingService;
	private final ExecutionClaimService claimService;
	private final ExecutorService workerExecutor;
	private final Worker worker;
	
	public JobScheduler(JobSchedulingService 
			jobSchedulingService,
			ExecutorService workerExecutor,
			Worker worker,
			 ExecutionClaimService claimService) {
		this.jobSchedulingService = jobSchedulingService;
		this.claimService = claimService;
		this.workerExecutor = workerExecutor;
		this.worker = worker;
	}
	
	/*@Scheduled(fixedDelay = 1000)
	public void tick() {
		jobSchedulingService.processDueJobs();
	}*/
	
	
	@Scheduled(fixedDelay = 1000)
	public void execute() {
		worker.runOnce(100);
	}
}
