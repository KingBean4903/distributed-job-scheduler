package com.example.demo.scheduler.execution.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.WebServer;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.execution.domain.JobExecution;
import com.example.demo.execution.executor.ExecutionResult;
import com.example.demo.execution.executor.HTTPJobExecutor;
import com.example.demo.job.Job;
import com.example.demo.job.JobType;
import com.example.demo.scheduling.scheduler.MisfirePolicy;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
public class HttpJobExecutionTest {
	
	private WireMockServer wireMockServer;
	private HTTPJobExecutor executor;
	
	@BeforeEach
	void setup() {
		wireMockServer = new WireMockServer(0);
		
		wireMockServer.start();
		
		executor= new HTTPJobExecutor(WebClient.builder());
	}
	
	@AfterEach
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldReturnSuccessFor2xx() {
		
		wireMockServer.stubFor(
				post(urlEqualTo("/jobs"))
						.willReturn(
								aResponse().withStatus(200)));
		
		Job job = createJob("/jobs", 5);
		
		JobExecution execution = 
				JobExecution.create(job.getId(), null);
		
		ExecutionResult result =
				executor.execute(job, execution);
		
		assertThat(result.success()).isTrue();
		
		assertThat(result.error()).isNull();
		
		
	}
	
	private Job createJob(String url, int timeoutSeconds) {
		
		Job job = Job.create(
				"test-http-job-1", 
				JobType.HTTP, 
				"*/5 * * * *", 
				"UTC", url, 
				MisfirePolicy.SKIP_MISSED, 
				3, 3, timeoutSeconds);
		
		return job;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
