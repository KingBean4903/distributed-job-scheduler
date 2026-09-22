package com.example.demo.scheduler.execution.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.example.demo.execution.executor.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.execution.executor.FakeJobExecutor;
import com.example.demo.execution.executor.JobExecutorRegistry;
import com.example.demo.job.JobType;

@SpringBootTest
public class JobExecutorRegistryTest {
	
	@Test
	void shouldReturnExecutorForSupportedJobType() {
		
		JobExecutor executor =
				new FakeJobExecutor();
		
		JobExecutorRegistry registry  =
				new JobExecutorRegistry(List.of(executor));
		
		JobExecutor result =
				registry.getExecutor(JobType.HTTP);
		
		assertThat(result).isSameAs(executor);
	}
	
	
	@Test
	void shouldFailWhenNoExecutorExists() {
		
		JobExecutorRegistry registry = 
				new JobExecutorRegistry(List.of());
		
		assertThatThrownBy(() -> registry.getExecutor(JobType.HTTP))
				.isInstanceOf(IllegalStateException.class);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
