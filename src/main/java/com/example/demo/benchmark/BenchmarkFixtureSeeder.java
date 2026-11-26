package com.example.demo.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BenchmarkFixtureSeeder implements BenchmarkFixtureSeederContract {
	
	private final JobCreationService jobCreationService;
	private final BenchmarkVerification verification;
	private final JobExecutionGenerationService executionGenerationService;
	
	private static final Logger log = LoggerFactory.getLogger(BenchmarkFixtureSeeder.class);
	
	public BenchmarkFixtureSeeder(
			JobCreationService jobCreationService,
			BenchmarkVerification verification,
			JobExecutionGenerationService executionGenerationService) {
		this.jobCreationService = jobCreationService;
		this.verification = verification;
		this.executionGenerationService = executionGenerationService;
		
	}
	

	@Override
	@Transactional
	public void seed(int jobCount) {
		
		if (jobCount <= 0 ) { 
			
			throw new IllegalArgumentException(""
					+ "jobCount  must b greater than 0");
			
		}
		
		if (verification.totalExecutions() != 0 ) {
			throw new IllegalStateException(
					"Benchmar db must b empty before seeding");
		}
	
		jobCreationService.createJobs(jobCount);
		
		long jActual = verification.totalJobs();
		
		log.info("Jobs {}", jActual);
		
		if (jActual != jobCount) {
			throw new IllegalArgumentException("Expected " + jobCount  +
					" jobs, but created " + jActual);
		}
		
		// executionGenerationService.generateExecutions();
		
/*		long actual = verification.totalExecutions();
		
		if (actual != jobCount) {
			throw new IllegalArgumentException("Expected " + jobCount  +
					" executions, but created " + actual);
		}
	
		long nonTerminal = verification.nonTerminalExecutions();
		
		if (nonTerminal != jobCount) { 
			throw new IllegalStateException("Expected " + jobCount 
					+ "non-terminal executions, but found"+ nonTerminal);
		}
		
		*/
	
	}

}
