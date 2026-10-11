package com.example.demo.load;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.execution.worker.Worker;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduler.execution.executor.config.MutableClock;
import com.example.demo.scheduler.execution.executor.config.TestClockConfig;
import com.example.demo.scheduling.calculator.ScheduleCalculator;
import com.example.demo.scheduling.misfire.MisfireDecision;
import com.example.demo.scheduling.misfire.MisfireHandler;
import com.example.demo.scheduling.scheduler.JobSchedulingService;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest(properties =  
"scheduler.worker.lease-duration-seconds:1")
@Import(TestClockConfig.class)
public class SchedulerLoadTest {

	private static final int JOB_COUNT = 1;
	
	private static final int BATCH_SIZE = 10;
	
	
	private static final String WORKER_ID = 
				"lease-worker-id";
	
	@Autowired
	ScheduleCalculator scheduleCalculator;
	
	@Autowired
	private MutableClock clock;
	
	@Autowired
	private Worker worker;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private ExecutionClaimService claimService;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	@Autowired
	JobSchedulingService schedulingService;
	
	private final String CONSTANT_DATE = "2026-10-09T12:38:00.00Z";
	
	@BeforeEach
	void setup() {
		executionRepository.deleteAllInBatch();
		jobRepository.deleteAllInBatch();
	}
	
	@Test
	void shouldProcessTenThousandJobs() throws InterruptedException {
		
		//createJob();
		
		assertThat(jobRepository.count())
						.isEqualTo(JOB_COUNT);
		
		//clock.advance(Duration.ofSeconds(10));
		
		System.out.println();
		
		System.out.println("Created " + JOB_COUNT + " jobs");
		
		//generateExecutions();
		
		//executionsGenerated.countDown();
		
		assertThat(executionRepository.count())
			.isEqualTo(JOB_COUNT);
		
		// Start Benchmark
		long start = System.nanoTime();
		
		//processJobs();
		
		long elapsedNanos = System.nanoTime() - start;
		
		double elapsedSeconds = 
				elapsedNanos / 1_000_000_000.00;
		
		long completed = 
				executionRepository.countByStatus(
						ExecutionStatus.SUCCESS);
		
		long failed = 
				executionRepository
					.countByStatus(ExecutionStatus.FAILED);
		
		long totalExecutions =
				executionRepository
					.count();
		
		double throughput =
					completed/ elapsedSeconds;
		
		// Assertions
		//assertThat(completed)
		//	.isEqualTo(JOB_COUNT);
		
		//assertThat(failed).isZero();
		
		//assertThat(totalExecutions)
			//	.isEqualTo(JOB_COUNT);
		
		// Print Benchmark
		System.out.println();
		
		System.out.println("===========================");
		System.out.println("SCHEDULER BASELINE");
		System.out.println("===========================");
		
		
		System.out.println(
				"Jobs                 "  + JOB_COUNT);
		System.out.println(
				"Executions           "  + totalExecutions);
		System.out.println(
				"Completed            "  + completed);
		System.out.println(
				"Failed               "  + failed);
		System.out.printf(
				"Duration             %.2f seconds%n", elapsedSeconds);
		System.out.printf(
				"Throughput           %.2f jobs/second%n", throughput);
		
		System.out.println("===========================");

	}

	private Job createJob(Instant scheduledAt) {
		
		
		

		
		
			
			Job job = Job.create(
					"load-test:" , 
					JobType.HTTP, 
					"*/5 * * * *", 
					"UTC", 
					"http://localhost:8800/load-test", 
					MisfirePolicy.SKIP_MISSED, 
					1, 
					0, 
					30);
			
			job.advanceNextRunAt(scheduledAt);


		return job;
		
		
		
	}
	
	@Test
	void shouldCreateExecutionAndAdvanceSchedule() { 
		
		Instant scheduledAt = Instant.parse(CONSTANT_DATE);
		
		Job job = createJob(scheduledAt);
		
		jobRepository.saveAndFlush(job);
		
		
		int processed = 
				schedulingService.processJobs(100);
		
		assertThat(processed).isEqualTo(1);
		
		List<JobExecution> executions = 
				executionRepository.findAll();
		
		assertThat(executions).hasSize(1);
		
		assertThat(executions.get(0).getScheduledAt())
				.isEqualTo(scheduledAt);
		
		Job updated = jobRepository.findById(
				job.getId()).orElseThrow();
		
		assertThat(updated.getNextRunAt())
			.isEqualTo(Instant.parse("2026-10-09T12:43:00.00Z"));
			
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
