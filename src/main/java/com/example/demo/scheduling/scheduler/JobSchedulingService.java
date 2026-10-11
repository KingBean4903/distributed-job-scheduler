package com.example.demo.scheduling.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.apache.el.stream.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobStatus;
import com.example.demo.scheduling.calculator.ScheduleCalculator;
import com.example.demo.scheduling.misfire.MisfireDecision;
import com.example.demo.scheduling.misfire.MisfireHandler;

@Service
public class JobSchedulingService {
	
	private JobRepository jobRepository;
	private JobExectionRepository executionRepository;
	private final SchedulingHook schedulingHook;
	private final ScheduleCalculator scheduleCalculator;
	private final MisfireHandler misfireHandler;
	private Clock clock;
	private final String CONSTANT_DATE = "2026-10-09T12:38:00.00Z";

	
	public JobSchedulingService(JobRepository jobRepository,
			JobExectionRepository executionRepository,
			ScheduleCalculator scheduleCalculator,
			SchedulingHook schedulingHook,
			MisfireHandler misfireHandler,
			Clock clock) {
		this.jobRepository = jobRepository;
		this.executionRepository = executionRepository;
		this.schedulingHook = schedulingHook;
		this.misfireHandler = misfireHandler;
		this.scheduleCalculator = scheduleCalculator;
		this.clock = clock;
	}
	
	@Transactional
	public void processJobs(UUID jobId) {
		
		Job job = jobRepository
					.findByIdForUpdate(jobId)
					.orElseThrow();
		
		Instant scheduledAt = job.getNextRunAt();
		
		if (scheduledAt == null) {
			return;
		}
		
		if (scheduledAt.isAfter(Instant.now())) {
			return;
		}
		
		JobExecution execution = 
				JobExecution.create(job.getId(), scheduledAt);
		
		executionRepository.save(execution);
		
		ZoneId zoneId = ZoneId.of(job.getTimezone());
		
		Instant nextRunAt = scheduleCalculator.nextExecution(
				job.getSchedule(), 
				scheduledAt, 
				zoneId);
		
		job.advanceNextRunAt(nextRunAt);
	}
	

	@Transactional
	public void processDueJobs() {
		
		Instant now = Instant.now(clock);
		
		List<Job> dueJobs = 
				jobRepository.findDueJobsForUpdate(100);
		
		schedulingHook.afterJobsLocked(dueJobs);
		
		for (Job job: dueJobs) {
			
			MisfireDecision decision = 
					misfireHandler.decide(job, now);
			
			for (Instant scheduledAt: decision.executions()) { 	
			
			JobExecution execution = JobExecution.create(
					job.getId(), scheduledAt);
			
			executionRepository.save(execution);
			
			}
			job.advanceNextRunAt(decision.nextRunAt());	
		}
	}
	
	
	@Transactional
	public int processJobs(int BATCH_SIZE) {
		
		List<Job> jobs = jobRepository.findDueJobsForUpdate(
				BATCH_SIZE, Instant.parse(CONSTANT_DATE));
		
		schedulingHook.afterJobsLocked(jobs);
		
		for (Job job: jobs) {
			Instant before = job.getNextRunAt();
			createExecution(job);
			Instant after  = job.getNextRunAt();
			
			
			System.out.println(
	                "job=" + job.getId()
	                + " before=" + before
	                + " after=" + after
	        );
		}
			
		return jobs.size()
;	}

	public void createExecution(Job job) {
		
		Instant now = Instant.now(clock);

		Instant scheduledAt = job.getNextRunAt();
		
		if (scheduledAt == null) {
			return;
		}
		
		if (scheduledAt.isAfter(Instant.parse(CONSTANT_DATE))) {
			return;
		}
		
		
		MisfireDecision decision = 
				misfireHandler.decide(job, now);
		
		for (Instant scheduled: decision.executions()) { 	
		
		JobExecution execution = JobExecution.create(
				job.getId(), scheduled);
		
	
		
		executionRepository.save(execution);
		
		ZoneId zoneId = ZoneId.of(job.getTimezone());
		
		Instant nextRunAt = scheduleCalculator.nextExecution(
				job.getSchedule(), 
				scheduled, 
				zoneId);
		
		job.advanceNextRunAt(nextRunAt);
		}
	}
	
	private void process(Job job, Instant now) {

		JobExecution execution = JobExecution.create(
				job.getId(), 
				job.getNextRunAt());
		
		executionRepository.save(execution);
		
		System.out.println("Create execution: " + execution.getId());
		
	}
}
