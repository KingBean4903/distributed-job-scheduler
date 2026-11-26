package com.example.demo.benchmark;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.scheduling.calculator.ScheduleCalculator;
import com.example.demo.scheduling.misfire.MisfireDecision;
import com.example.demo.scheduling.misfire.MisfireHandler;
import com.example.demo.scheduling.scheduler.SchedulingHook;

@Service
public class JobExecutionGenerationService {

	private final JobRepository jobRepository;
	private final JobExectionRepository executionRepository;
	private final SchedulingHook schedulingHook;
	private final MisfireHandler misfireHandler;
	public JobExecutionGenerationService(
			JobRepository jobRepository,
			JobExectionRepository executionRepository,
			SchedulingHook schedulingHook,
			MisfireHandler misfireHandler) {
		this.jobRepository = jobRepository;
		this.schedulingHook = schedulingHook;
		this.misfireHandler = misfireHandler;
		this.executionRepository = executionRepository;
	}
	
	@Transactional
	public void generateExecutions() {
		
		
		Instant now = Instant.now();

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
}
