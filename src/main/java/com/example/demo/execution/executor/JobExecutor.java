package com.example.demo.execution.executor;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;

public interface JobExecutor {

	boolean supports(JobType jobType);
	
	ExecutionResult execute(
			Job job, 
			JobExecution executor);
}
