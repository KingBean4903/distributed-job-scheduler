package com.example.demo.benchmark;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@Service
public class JobCreationService {
	
	// private final String CONSTANT_DATE = "2026-10-12T15:40:00.00Z";
	
	private final JobRepository jobRepository;
	
	private final JobExectionRepository executionRepository;

	
	public JobCreationService(JobRepository jobRepository,
		 JobExectionRepository executionRepository
) {
		this.jobRepository = jobRepository;
		
		this.executionRepository = executionRepository;
	}
	
	@Transactional
	public void createJobs(int count) { 
		
		List<Job> jobs = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			Job job =Job.create(
					"benchmark:" + i, 
					JobType.HTTP, 
					"*/5 * * * *", 
					"UTC", 
					"http://172.17.0.1:8800/load-test", 
					MisfirePolicy.SKIP_MISSED, 
					1, 
					0, 
					30);
			
			job.advanceNextRunAt(Instant.now());
			
			jobs.add(job);
		}
		
		jobRepository.saveAll(jobs);
		jobRepository.flush();
		
		List<JobExecution> executions = jobs.stream()
				.map(job -> JobExecution.create(job.getId(), 
						Instant.now())).toList();
		
		executionRepository.saveAll(executions);
		executionRepository.flush();
		
	}

}
