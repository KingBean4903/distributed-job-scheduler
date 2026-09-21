package com.example.demo.scheduling.scheduler;

import java.util.List;

import com.example.demo.job.Job;

public interface SchedulingHook {

	void afterJobsLocked(List<Job> jobs);
}
