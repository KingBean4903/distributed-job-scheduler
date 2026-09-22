package com.example.demo.scheduler.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobStatus;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest
public class JobTests {
	
	@Autowired
	JobRepository jobRepository;
	
	@Test
	void shouldCreateActiveJob() {
		Job job = Job.create(
				"generate-report", 
				JobType.HTTP, 
				"0 */5 * * * *", 
				"UTC",
				"{\"customerId\": 123}", 
				MisfirePolicy.SKIP_MISSED,
				5, 
				3, 
				30);
		
		assertThat(job.getStatus())
					.isEqualTo(JobStatus.ACTIVE);
	}

	@Test 
	public void shouldPauseActiveJob() {
		Job job = createJob();
		
		job.pause();
		
		assertThat(job.getStatus())
				.isEqualTo(JobStatus.PAUSED);
	}
	
	@Test
	void shouldNotResumeCancelledJob() {
		
		Job job= createJob();
		job.cancel();
		
		assertThatThrownBy(job::resume)
			.isInstanceOf(IllegalStateException.class);
	}
	
	@Test
	void shouldPersistJob() {
		
		Job job = createJob();
		
		Job saved = jobRepository.save(job);
		
		assertThat(saved.getId())
			.isNotNull();
		
		assertThat(jobRepository.findById(saved.getId()))
				.isPresent();
		
		
	}
	
	
	
	
	
	private Job createJob() {
		
		return Job.create(
				"generate-report", 
				JobType.HTTP, 
				"0 */5 * * * *", 
				"UTC",
				"{\"customerId\": 123}", 
				MisfirePolicy.SKIP_MISSED,
				5, 
				3, 
				30);
	}
}
