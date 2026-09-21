package com.example.demo.scheduling.scheduler;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.job.Job;

@Component
public class NoOpSchedulingHook implements SchedulingHook {

	@Override
	public void afterJobsLocked(List<Job> jobs) {
		// TODO Auto-generated method stub
	}
	
	
}
