package com.example.demo.scheduling.misfire;

import java.time.Instant;
import java.util.List;

import com.example.demo.job.Job;

public interface MisfireHandler {
	
	MisfireDecision decide(
			Job job,
			Instant now);
}
