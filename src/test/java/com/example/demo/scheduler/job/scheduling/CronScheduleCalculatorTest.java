package com.example.demo.scheduler.job.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.example.demo.scheduling.calculator.CronScheduleCalculator;

public class CronScheduleCalculatorTest {

	private final CronScheduleCalculator calculator =
			new CronScheduleCalculator();
	
	private final ZoneId zone = ZoneOffset.UTC;
	
	@Test
	void shouldCalculateNextFiveMinuteOccurrence() {
		
		Instant current = 
				Instant.parse("2026-09-11T10:02:00Z");
		
		Instant next =
				calculator.nextExecution(
						"*/5 * * * *", current, zone);
		
		assertThat(next)
			.isEqualTo(Instant.parse(
					"2026-09-11T10:05:00Z"));	
	}
	
	@Test
	void shouldCalculateNextHourlyOccurrence() { 
		
		Instant  current = 
				Instant.parse("2026-09-11T10:15:00Z");
		
		Instant next =
				calculator.nextExecution(
						"0 * * * *", current, zone);
		
		 assertThat(next)
		 		.isEqualTo(Instant.parse("2026-09-11T11:00:00Z"));
				
	}
	
	@Test
	void shouldSupportSixFieldCron() {
		
		Instant current =
				Instant.parse("2026-09-11T10:02:00Z");
		
		Instant next =
				calculator.nextExecution(
						"30 */5 * * * *", 
						current, zone);
		
		assertThat(next)
				.isEqualTo(
						Instant.parse("2026-09-11T10:05:30Z"));
	}
	
	@Test
	void shouldRejectInvalidCronExpression() {
		
		assertThatThrownBy(() -> 
				calculator.nextExecution(
						"invalid",
						Instant.now(),
						zone))
		.isInstanceOf(RuntimeException.class);
	}
	
	@Test
	void shouldCalculateNextFiveMiutesOccurrence() {
		
		Instant current =
				Instant.parse("2026-09-12T10:02:00Z");
		
		Instant next = 
				 calculator.nextExecution(
						 "*/5 * * * *", current, ZoneOffset.UTC);
		
		assertThat(next)
			.isEqualTo(Instant.parse(
					"2026-09-12T10:05:00Z"));
	}
	
	@Test
	void shouldCalculateNextWeeklyOccurrence() {
		
		Instant current = 
				Instant.parse("2026-09-12T10:00:00Z");
		
		Instant next =
				calculator.nextExecution(
						"0 9 * * MON", 
						current, 
						ZoneOffset.UTC);
		
		assertThat(next)
				.isEqualTo(
						Instant.parse("2026-09-14T09:00:00Z"));
		
	}
	
	@Test
	void shouldCalculateUsingJobTimezone() {
		
		Instant current =
				Instant.parse("2026-09-12T05:00:00Z");
		
		Instant next = 
				calculator.nextExecution(
						"0 9 * * *", current, 
						ZoneId.of("Africa/Nairobi"));
		
		assertThat(next)
			.isEqualTo(
					Instant.parse("2026-09-12T06:00:00Z"));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
