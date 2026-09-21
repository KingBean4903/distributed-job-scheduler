package com.example.demo.scheduler.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;

@SpringBootTest
public class JobExectionTest {
	
	@Test
	void shouldStartReadyExecution() {
		UUID jobId = UUID.randomUUID();
		
		JobExecution execution = 
				JobExecution.create(
						jobId,
						Instant.now());
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThat(execution.getStatus())
				.isEqualTo(ExecutionStatus.RUNNING);
		
		assertThat(execution.getWorkerId())
				.isEqualTo("worker-1");
		
		assertThat(execution.getAttempt())
				.isEqualTo(1);
		
	}
	
	@Test
	void shouldNotStartExecutionTwice() {
		
		JobExecution execution =
				JobExecution.create(UUID.randomUUID(), Instant.now());
		
		execution.start("worker-1", Instant.now().plusSeconds(30));
		
		assertThatThrownBy(() -> 
					execution.start("worker-2",
							Instant.now().plusSeconds(30)))
		.isInstanceOf(IllegalStateException.class);

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
