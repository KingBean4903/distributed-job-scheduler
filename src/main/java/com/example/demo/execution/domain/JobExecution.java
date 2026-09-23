package com.example.demo.execution.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name="job_executions")
public class JobExecution {

	@Id
	@GeneratedValue(strategy=GenerationType.UUID)
	private UUID id;
	
	@Column(name="job_id",
			nullable=false)
	private UUID jobId;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private ExecutionStatus status;
	
	@Column(nullable=false)
	private int attempt;
	
	@Column(name="next_attempt_at")
	private Instant nextAttemptAt;
	
	@Column(name="scheduled_at", nullable=false)
	private Instant scheduledAt;
	
	@Column(name="started_at")
	private Instant startedAt;
	
	@Column(name="completed_at")
	private Instant completedAt;
	
	@Column(name="worker_id")
	private String workerId;
	
	@Column(name="lease_expires_at")
	private Instant leaseExpiresAt;
	
	@Column(columnDefinition="TEXT")
	private String error;
	
	@Column(nullable=false,updatable=false)
	private Instant createdAt;
	
	protected JobExecution() {}
	
	private JobExecution(
			UUID jobId,
			Instant scheduledAt) {
		
		this.jobId = jobId;
		this.scheduledAt = scheduledAt;
		this.status = ExecutionStatus.READY;
		this.attempt = 0;
		this.nextAttemptAt = scheduledAt;
		this.createdAt = Instant.now();
	}
	
	public static JobExecution create(
			UUID jobId, Instant scheduledAt) {
		return new JobExecution(jobId, scheduledAt);
	}
	
	public void start(String workerId,
			Instant leaseExpiresAt) {
		
		if (status != ExecutionStatus.READY) {
			
			throw new IllegalStateException("Execution not ready");
		}
		this.status = ExecutionStatus.RUNNING;
		this.workerId = workerId;
		this.startedAt = Instant.now();
		this.leaseExpiresAt = leaseExpiresAt;
		this.attempt++;
	}
	
	public void succeed() {
		if (status != ExecutionStatus.RUNNING) {
			throw new IllegalStateException(
					"Only running executions can succeed");
		}
		
		this.status =ExecutionStatus.SUCCESS;
		this.completedAt = Instant.now();
	}
	
	public void fail(String error) {
		if (status != ExecutionStatus.RUNNING) {
			throw new IllegalStateException("Only running exceptions can fail");
		}
		this.status = ExecutionStatus.FAILED;
		this.error = error;
		this.completedAt = Instant.now();
	}
	
	public void shouldRetry(Instant nextAttemptAt) {
		
		if (status != ExecutionStatus.FAILED) {
			throw new IllegalStateException(
					"Only failed executions can be retried");
		}
		
		if (nextAttemptAt == null) {
			throw new IllegalArgumentException(
					"Not attempt time can be null");
		}
		
		status= ExecutionStatus.READY;
		this.nextAttemptAt = nextAttemptAt;
		workerId = null;
		leaseExpiresAt = null;
		startedAt = null;
		completedAt = null;
	}
	
	public void moveToDlq(String reason) {
		if (status != ExecutionStatus.FAILED) {
			throw new IllegalStateException(
					"Only failed executions are moved to dlq");
		}
		
		status = ExecutionStatus.DLQ;
		error = reason;
	}
	
	
	public UUID getJobId() { return jobId; }
	public int getAttempt() {  return attempt; }
	public String getWorkerId() { return workerId; }
	public ExecutionStatus getStatus() { return status; }
	public Instant getScheduledAt() { return scheduledAt; }
	public Instant getStartedAt() { return startedAt; }
	public Instant getCompletedAt() { return completedAt; }
  	public Instant getLeaseExpiresAt() { return leaseExpiresAt; }
	public UUID getId() { return id; }
	public Instant getNextAttemptAt() { return nextAttemptAt; }
	
}
