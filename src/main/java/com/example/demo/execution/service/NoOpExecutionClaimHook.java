package com.example.demo.execution.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.execution.domain.JobExecution;

@Component
public class NoOpExecutionClaimHook implements
	ExecutionClaimHook {

	@Override
	public void afterExecutionClaimed(List<JobExecution> executions) {
		// TODO Auto-generated method stub
		
	}

}
