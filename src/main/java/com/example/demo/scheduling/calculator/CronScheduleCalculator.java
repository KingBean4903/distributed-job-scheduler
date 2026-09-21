package com.example.demo.scheduling.calculator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

@Component
public class CronScheduleCalculator implements ScheduleCalculator {

	@Override
	public Instant nextExecution(
			String schedule, 
			Instant countExecution, 
			ZoneId zoneId) {
		
		CronExpression cron = CronExpression.parse(normalize(schedule));
		
		ZonedDateTime next = cron.next(cron);
		
		if (next == null) {
			throw new IllegalStateException(
					"NO next execution exists for scheduler: "+ schedule);
		}
		return next.toInstant();
	}
	
	private String normalize(String schedule) {
		String[] fields = 
					schedule.trim().split("\\s+");
		if (fields.length == 5) {
			return "0" + schedule;
		}
		
		if (fields.length == 6) {
			return schedule;
		}
		
		
		throw new IllegalArgumentException(
				"Cron expression must contain 5 or 6 fields" + schedule);
	}

}
