package com.example.demo.scheduling.scheduler;

import java.time.Instant;
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

@Service
public class JobSchedulingService {
	
	private JobRepository jobRepository;
	private JobExectionRepository executionRepository;
	private final SchedulingHook schedulingHook;
	
	public JobSchedulingService(JobRepository jobRepository,
			JobExectionRepository executionRepository,
			SchedulingHook schedulingHook) {
		this.jobRepository = jobRepository;
		this.executionRepository = executionRepository;
		this.schedulingHook = schedulingHook;
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
		
		job.advanceNextRunAt(scheduledAt.plusSeconds(300));
	}

	@Transactional
	public void processDueJobs() {
		
		Instant now = Instant.now();
		
		List<Job> dueJobs = 
				jobRepository.findDueJobsForUpdate(100);
		
		schedulingHook.afterJobsLocked(dueJobs);
		
		
		for (Job job: dueJobs) {
			
			Instant scheduledAt = 
						job.getNextRunAt();
			
			JobExecution executor = JobExecution.create(
					job.getId(), scheduledAt);
			
			executionRepository.save(executor);
			Instant nextRun = 
					scheduledAt.plusSeconds(300);
			
			job.advanceNextRunAt(nextRun);
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
