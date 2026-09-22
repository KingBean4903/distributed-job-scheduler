package com.example.demo.execution.executor;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;

public class FakeJobExecutor implements JobExecutor {

	@Override
	public boolean supports(JobType jobType) {
		return jobType == JobType.HTTP;
	}

	@Override
	public ExecutionResult execute(Job job, JobExecution executor) {
		System.out.println("Executing job " + job.getName());
		return ExecutionResult.succeed();
	}

}
