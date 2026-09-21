package com.example.demo.scheduling.scheduler;

import java.time.Instant;

public interface SchedulerCalculator {
	
	Instant nextExecution(
			String schedule,
			Instant currentTime);
}
