package com.example.demo.execution.executor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.JobType;

@Component
public class JobExecutorRegistry {

	private final List<JobExecutor> executors;
	
	public JobExecutorRegistry(
			List<JobExecutor> executors) {
		this.executors = executors;
	}
	
	public JobExecutor getExecutor(JobType jobType) {
		
		return executors.stream()
				.filter(executor -> 
						executor.supports(jobType))
				.findFirst()
				.orElseThrow(() -> 
						new IllegalStateException(
								"NO execution found for job type" + jobType));
	}
}
