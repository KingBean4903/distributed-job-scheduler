package com.example.demo.scheduling.calculator;

import java.time.Instant;
import java.time.ZoneId;

public interface ScheduleCalculator {
	
	Instant nextExecution(
			String schedule,
			Instant countExecution,
			ZoneId zoneId);

}
