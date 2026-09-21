package com.example.demo.scheduling.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

	private final JobSchedulingService 
	 jobSchedulingService;
	
	public JobScheduler(JobSchedulingService 
			jobSchedulingService) {
		this.jobSchedulingService = jobSchedulingService;
	}
	
	@Scheduled(fixedDelay = 1000)
	public void tic() {
		jobSchedulingService.processDueJobs();
	}
}
