package com.example.demo.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.repository.JobExectionRepository;
import com.example.demo.execution.service.ExecutionClaimService;
import com.example.demo.execution.worker.Worker;
import com.example.demo.job.Job;
import com.example.demo.job.JobRepository;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SchedulerConcurrencyBenchmarkTest {
	
	private static final Logger log = LoggerFactory.getLogger(SchedulerConcurrencyBenchmarkTest.class);

	
	private static final int JOB_COUNT = 10_000;
	
	private static final int BATCH_SIZE= 10;

	private static final String claimMs = null;
	
	private final String CONSTANT_DATE = "2026-10-12T15:40:00.00Z";
	
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private ConcurrentHashMap<String, Map<String, Double>> stats
			= new ConcurrentHashMap<>();
	
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobExectionRepository executionRepository;
	
	@Autowired
	private ExecutionClaimService claimService;
	
	@Autowired
	Worker worker;
	
	@BeforeEach
	void setup() {
		jobRepository.deleteAll();
		executionRepository.deleteAll();
	}
	
	@Test
	void shouldBenchmarkSingleWorker() {
		
		prepareExecutions();
		
		long start = System.nanoTime();
		
		processJobs(16);
		
		long elapsedNanos = System.nanoTime() - start;
		
		long completed = executionRepository.countByStatus(
				ExecutionStatus.SUCCESS);
		
		double elapsedSeconds = elapsedNanos / 1_000_000_000.00;
		
		double throughput = completed / elapsedSeconds;
		
		System.out.printf("worker :%d execution %d elapsed %.3fs throughput %.2f/sec%n",
				16, completed, elapsedSeconds, throughput);
		
		
		assertThat(completed).isEqualTo(JOB_COUNT);
		
	}
	
	@Test
	void shouldCreateReadyJobs( ) { 
		
		List<Job> jobs = new ArrayList<>();
		
		for (int i = 0; i < JOB_COUNT; i++) {
			Job job =Job.create(
					"benchmark:" + i, 
					JobType.HTTP, 
					"*/5 * * * *", 
					"UTC", 
					"http://localhost:8800/load-test", 
					MisfirePolicy.SKIP_MISSED, 
					1, 
					0, 
					30);
			
			// job.advanceNextRunAt(Instant.parse(CONSTANT_DATE));
			
			jobs.add(job);
		}
		
		jobRepository.saveAll(jobs);
		jobRepository.flush();
		
		
		List<JobExecution> executions = jobs.stream()
				.map(job -> JobExecution.create(job.getId(), 
						Instant.parse(CONSTANT_DATE))).toList();
		
		executionRepository.saveAll(executions);
		executionRepository.flush();
		
		
		assertThat(jobRepository.count()).isEqualTo(JOB_COUNT);
		
		assertThat(executionRepository.count()).isEqualTo(JOB_COUNT);
		
		
	}
	
	
	private void prepareExecutions() {
		
		
		
		List<Job> jobs = new ArrayList<>();
		
		for (int i = 0; i < JOB_COUNT; i++) {
			Job job =Job.create(
					"benchmark:" + i, 
					JobType.HTTP, 
					"*/5 * * * *", 
					"UTC", 
					"http://localhost:8800/load-test", 
					MisfirePolicy.SKIP_MISSED, 
					1, 
					0, 
					30);
			
			
			jobs.add(job);
		}
		
		jobRepository.saveAll(jobs);
		jobRepository.flush();
		
		
		List<JobExecution> executions = jobs.stream()
				.map(job -> JobExecution.create(job.getId(), 
						Instant.now())).toList();
		
		executionRepository.saveAll(executions);
		executionRepository.flush();
		
		
		assertThat(jobRepository.count()).isEqualTo(JOB_COUNT);
		
		assertThat(executionRepository.count()).isEqualTo(JOB_COUNT);
		
	}
	
	private void processJobs(int workerCount) {
		
		ExecutorService executor =
				Executors.newFixedThreadPool(workerCount);
		
		LatencyRecorder claimLatency = 
				new LatencyRecorder();
		
		LatencyRecorder completionLatency = 
				new LatencyRecorder();
		
		LatencyRecorder jobLatency = 
				new LatencyRecorder();
		
		long claimCount = 0;
		long benchmarkStart = System.nanoTime();
		
		try {
			
			while (true) {
				
				long start = System.nanoTime();
				
				
				List<JobExecution> executions = 
						claimService.claim("benchmark-worker", BATCH_SIZE);
				
				long claimDuration = System.nanoTime() - start;
				
				claimLatency.record(claimDuration);
				claimCount++;
				
				
				if (!executions.isEmpty()) {
					
					for (JobExecution execution: executions) {
						executor.submit(() -> {
							
							worker.execute(
									execution, 
									stats, 
									completionLatency,
									jobLatency);
							
						});
					}
					
				}
				
			long ready = executionRepository.countByStatus(
					ExecutionStatus.READY);
			
			long running = executionRepository.countByStatus(
					ExecutionStatus.RUNNING);
			
			
			
			if (ready == 0 && running == 0) {
				
				calculateMedians();
				
				break;
			}
			
				
			} 
		} finally {
			executor.shutdown();
			try {
				
				boolean finished =
						executor.awaitTermination(10, TimeUnit.MINUTES);
				
				assertThat(finished)
						.as("Workers did not finish within timeout")
						.isTrue();
				
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RuntimeException(e);
			}
			
		}
		
		double elapsedSeconds = (System.nanoTime() - benchmarkStart) / 1_000_000_000.00;
		
		System.out.printf("worker=%d elapsed=%.2fs throughput=%.2f jobs/sec",
				workerCount, elapsedSeconds, 10_000 / elapsedSeconds);
		
		System.out.printf("claimCount=%d medianClaim=%.3fms p95Claim=%.3fms%n",
				claimCount, claimLatency.medianMs(), claimLatency.p95Ms());
		
		System.out.printf("medianCompletion=%.3fms p95Completion=%.3fms%n",
				 completionLatency.medianMs(), completionLatency.p95Ms());
		
		System.out.printf("medianJobLookup=%.3fms p95JobLookup=%.3fms%n",
				 jobLatency.medianMs(), jobLatency.p95Ms());
		
	}
	
	private void calculateMedians() {
		
		List<Double> lookups = stats.entrySet().stream()
									.map(one -> one.getValue())
									.filter(one -> one.containsKey("lookup"))
									.map(one -> one.get("lookup"))
									.collect(Collectors.toList());
		
		assertThat(lookups.size()).isEqualTo(10_000);
		
		Double lookupNanos = medianCalc(lookups);
		
		List<Double> works = stats.entrySet().stream()
				.map(one -> one.getValue())
				.filter(one -> one.containsKey("work"))
				.map(one -> one.get("work"))
				.collect(Collectors.toList());
		
		Double workNanos = medianCalc(works);
		
		List<Double> completion = stats.entrySet().stream()
				.map(one -> one.getValue())
				.filter(one -> one.containsKey("completion"))
				.map(one -> one.get("completion"))
				.collect(Collectors.toList());
		
		Double completionNanos = medianCalc(completion);
		
		
		List<Double> totals = stats.entrySet().stream()
				.map(one -> one.getValue())
				.filter(one -> one.containsKey("total"))
				.map(one -> one.get("total"))
				.collect(Collectors.toList());
		
		Double totalNanos = medianCalc(totals);
		
		
		System.out.printf(
		"Median stats lookupMs= %.2f workMs=  %.2f completionMs= %.2f totalMs= %.2f%n",
		lookupNanos,
		workNanos,
		completionNanos,
		totalNanos);
				
		
		
	}

	
	public Double medianCalc(List<Double> lList) {
		
		
		int midIdx =  (lList.size()/2) - 1;
		
		double midV = lList.get(midIdx);
		double midV2 = lList.get(midIdx + 1);
		
		double median = (midV + midV2) / 2;
		
		return median;
	}
}
