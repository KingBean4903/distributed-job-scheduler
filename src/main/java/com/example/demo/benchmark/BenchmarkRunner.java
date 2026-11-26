package com.example.demo.benchmark;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.demo.execution.worker.Worker;

@Component
@ConditionalOnProperty(
		prefix = "scheduler.benchmark",
		name="enabled",
		havingValue="true")
public class BenchmarkRunner implements CommandLineRunner {
	
	
	private final BenchmarkProperties properties;
	private final BenchmarkFixtureSeederContract seeder;
	private final BenchmarkVerification verification;
	private final Worker worker; 
	
	public BenchmarkRunner(
			  BenchmarkProperties properties,
			  BenchmarkFixtureSeederContract seeder,
			  BenchmarkVerification verification,
			  Worker worker) {
			this.properties = properties;
			this.seeder = seeder;
			this.worker = worker;
			this.verification = verification;
	}

	@Override
	public void run(String... args) throws Exception {
		
		String mode = args.length == 0 ? "monitor" : args[0];
		
		switch(mode) {
		case "seed" -> seed();
		case "monitor" -> monitor();
		default -> throw new IllegalArgumentException(
				"Expected mode: seed or monitor");
		}
		
	}
	
	
	
	private void seed() {
		
		long started = System.nanoTime();
		
		seeder.seed(properties.jobCount());
		
		long elapsed = System.nanoTime() - started;
		
		long total  = verification.totalJobs();
		
/*		long nonTerminal = verification.nonTerminalExecutions();
		
		if (total != properties.jobCount()) {
			throw new IllegalStateException(
					"Expected " + properties.jobCount() +
					"executions, found " + total);
		}
		
		if (nonTerminal != properties.jobCount()) {
			
			throw new IllegalStateException(""
					+ "Expected all executions to be non terminal"
					+ "before " + " the drain starts; found" + nonTerminal);

		}
	*/	
		System.out.printf("SEED_OK jobs=%d elapsed=%.3fs%n", total, elapsed/ 1_000_000_000.00);
		
	}
	
	
	private void monitor() throws Exception {
		
		
		long started = System.nanoTime();
		
		Instant deadline = Instant.now().plus(properties.timeout());
		
		while (Instant.now().isBefore(deadline)) {
			
//			long total  = verification.totalExecutions();
			
			long pending = verification.nonTerminalExecutions();
			
/*			if (total != properties.jobCount()) { 
				
				throw new IllegalArgumentException(
						"Execution count changed: expected"
						+ properties.jobCount() + " found,  "  + total);
				
			}
			*/
			
			if (pending == 0) {
				reportAndVerify(started);
				return;
			}
			
			TimeUnit.MILLISECONDS.sleep(
					properties.pollInterval().toMillis());

		}
		
		Map<String, Long> counts = 
				verification.countByStatus();
		
		double claimLatency = median(worker.getClaims());
	
	throw new IllegalArgumentException( 
			"BENCHMARK_TIMEOUT after " + 
				properties.timeout() + " status counts" + counts + "Claim latency: " + claimLatency + "s");
		
	}
	

	private void reportAndVerify(long started) {
		
		Map<String, Long> counts = 
				verification.countByStatus();
		
		long total = verification.totalExecutions();
		
		long succeeded = counts.getOrDefault("SUCCESS", 0L);
		
		long failed = counts.getOrDefault("FAILED", 0L);
		
		
		long unfinishedExecutions = verification.nonTerminalExecutions();
				
		double claimLatency = median(worker.getClaims());
		
		double elapsedSeconds = 
				(System.nanoTime() - started) / 1_000_000_000.00;
		
		if (unfinishedExecutions != 0) {
				throw new IllegalStateException(  
						"Benchmark incomplete "  + unfinishedExecutions);
		}
		
		if (failed != 0) {
			throw new IllegalStateException( 
					"JObs failed: " + failed + "counts " + counts);
		}
		
		System.out.printf(
				"BENCHMARK_OK "
				+ "total=%d"
				+ "succeeded=%d"
				+ "failed=%d"
				+ "elapsed=%.3fs"
				+ "throughput=%.2f jobs/sec"
				+ "counts = %s%n"
				+ "claim latency = %.2fs", 
				total, succeeded, failed,
				elapsedSeconds, total/elapsedSeconds,
				counts, claimLatency);
	}
	
	public Double median(List<Double> list) {
		
		int midIdx = ((list.size()) / 2) - 1;
		double midV1 = list.get(midIdx);
		double midV2 = list.get(midIdx + 1);
		
		return (midV1 + midV2) / 2.00;
		
	}
	
}
