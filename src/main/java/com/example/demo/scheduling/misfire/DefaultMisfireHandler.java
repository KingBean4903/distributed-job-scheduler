package com.example.demo.scheduling.misfire;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.job.Job;
import com.example.demo.scheduling.calculator.ScheduleCalculator;

@Component
public class DefaultMisfireHandler implements MisfireHandler {
	
	private final ScheduleCalculator scheduleCalculator;
	
	public DefaultMisfireHandler(
			ScheduleCalculator scheduleCalculator) {
		this.scheduleCalculator = scheduleCalculator;
	}

//	public List<Instant> executionsToCreate(Job job, Instant now) {
//		
//		Instant scheduleAt = job.getNextRunAt();
//		
//		return switch(job.getMisfirePolicy()) {
//		case SKIP_MISSED -> List.of(findNextFutureOccurrence(job, now));
//		case CATCH_UP -> calculateAllMissedOccurrence(job, now);
//		case LATEST -> List.of(findLatestMissedOccurrence(job,now));
//		};
//	}
//	
//	private Instant findNextFutureOccurrence(Job job, Instant now) {
//		
//		Instant occurrence = job.getNextRunAt();
//		
//		while(!occurrence.isAfter(now)) {
//			occurrence = scheduleCalculator.nextExecution(
//					job.getSchedule(), 
//					occurrence, 
//					ZoneId.of(job.getTimezone()));
//					
//		}
//		return occurrence;
//	}
//
//	private List<Instant> calculateAllMissedOccurrence(Job job, Instant now) {
//		
//		List<Instant> occurrences = new ArrayList<>();
//		
//		Instant occurrence = job.getNextRunAt();
//		while (!occurrence.isAfter(now)) {
//			
//			occurrences.add(occurrence);
//			occurrence = scheduleCalculator.nextExecution(
//					job.getSchedule(), occurrence, ZoneId.of(job.getTimezone()));
//		}
//		return occurrences;
//	}
//	
//	private Instant findLatestMissedOccurrence(Job job, Instant now) {
//		
//		Instant occurrence = job.getNextRunAt();
//		
//		Instant latest = occurrence;
//		
//		while(!occurrence.isAfter(now)) {
//			latest = occurrence;
//			
//			occurrence = scheduleCalculator.nextExecution(
//					job.getSchedule(), 
//					occurrence, 
//					ZoneId.of(job.getTimezone()));
//		}
//		
//		return latest;
//	}

	@Override
	public MisfireDecision decide(Job job, Instant now) {
		
		ZoneId zone =
				ZoneId.of(job.getTimezone());
		
		return switch(job.getMisfirePolicy()) {
		case SKIP_MISSED -> 
				skipMissed(job, now, zone);
		case CATCH_UP ->
				catchUp(job, now, zone);
		case LATEST ->
				latest(job, now, zone);
		};
	}
	
	private MisfireDecision skipMissed(
			Job job,
			Instant now,
			ZoneId zone) {
		
		Instant occurrence = job.getNextRunAt();
		
		while (!occurrence.isAfter(now)) {
			occurrence = next(job, occurrence, zone);
		}
		
		return new MisfireDecision(List.of(occurrence), next(job, occurrence, zone));
	}
	
	private MisfireDecision catchUp(
			Job job, 
			Instant now, 
			ZoneId zone) {
		
		List<Instant> executions = 
					new ArrayList<>();
		
		Instant occurrence = job.getNextRunAt();
		
		while (!occurrence.isAfter(now)) {
			executions.add(occurrence);
			
			occurrence = next(job, occurrence, zone);
		}
		
		return new MisfireDecision(executions, occurrence);
	}
	
	private MisfireDecision latest(
			Job job, 
			Instant now,
			ZoneId zone) {
		
		Instant occurrence = job.getNextRunAt();
		
		Instant latest = null;
		
		while(!occurrence.isAfter(now)) {
			
			latest = occurrence;
			occurrence = next(job, occurrence, zone);
			
		}
		
		if (latest == null) {
			return new MisfireDecision(
					List.of(), occurrence);
		}
		
		return new MisfireDecision(
				List.of(latest), occurrence);
	}
	
	private Instant next(
			Job job,
			Instant occurrence,
			ZoneId zone) {
		
		return scheduleCalculator.nextExecution(
				job.getSchedule(),
				occurrence,
				zone);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
