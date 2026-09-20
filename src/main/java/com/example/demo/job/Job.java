package com.example.demo.job;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name="jobs")
public class Job {
	
	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	@Column(nullable=false)
	private String name;
	
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private JobType type;
	
	@Column(nullable=false,
			columnDefinition="TEXT")
	private String schedule;
	
	@Column(columnDefinition="TEXT")
	private String payload;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private JobStatus status;
	
	@Column(nullable=false)
	private Integer priority;
	
	@Column(name="max_retries", nullable=false)
	private Integer maxRetries;
	
	@Column(name="timeout_seconds", nullable=false)
	private Integer timeoutSeconds;
	
	@Column(name="next_run_at")
	private Instant nextRunAt;
	
	@Column(nullable=false, updatable=false)
	private Instant createdAt;
	
	@Column(nullable=false)
	private Instant updatedAt;
	
	protected Job() {}
	
	private Job(
			String name,
			JobType type, 
			String schedule,
			String payload,
			Integer priority,
			Integer maxRetries,
			Integer timeoutSeconds) {
		
		this.name = name;
		this.type = type;
		this.schedule = schedule;
		this.payload = payload;
		this.priority = priority;
		this.maxRetries = maxRetries;
		this.timeoutSeconds = timeoutSeconds;
	}
	
	public static Job create(
			String name,
			JobType type,
			String schedule,
			String payload,
			Integer priority,
			Integer maxRetries,
			Integer timeoutSeconds
			) { 
		
		return new Job(name, type,
				schedule, payload,
				priority,  maxRetries,
				timeoutSeconds);
	}
	
	
	// Lifecycle behaviour
	public void pause() {
		if (status == JobStatus.CANCELLED) {
			
			throw new IllegalStateException(
					"Cancelled job must not be found");
		}
		status = JobStatus.PAUSED;
		updatedAt = Instant.now();
	}
	
	public void resume() {
		if (status == JobStatus.CANCELLED) {
			throw new IllegalStateException(
					"Cancelled job cannot be found");
		}
		status = JobStatus.ACTIVE;
		updatedAt = Instant.now();
	}
	
	public void cancel() {
		if (status == JobStatus.CANCELLED) {
			return;
		}
		status = JobStatus.CANCELLED;
		updatedAt = Instant.now();
	}
		
	public String getName() { return name; }
	public JobStatus getStatus() { return status; }
	public String getSchedule() { return schedule; }		
	public String getPayload() { return payload; }
	public 	Integer getPriority() { return priority; }	
	public Integer getTimeoutSeconds() { return timeoutSeconds; }
	public Integer getMaxRetries()  { return maxRetries; } 
					
}
