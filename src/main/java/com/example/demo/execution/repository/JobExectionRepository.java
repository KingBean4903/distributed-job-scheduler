package com.example.demo.execution.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;
import com.example.demo.job.Job;

public interface JobExectionRepository extends JpaRepository<JobExecution, UUID> {
	
	
	@Query(value = """
			SELECT *
			FROM job_executions
			WHERE status = 'READY'
			AND next_attempt_at <= NOW()
			ORDER BY next_attempt_at ASC
			LIMIT :limit
			FOR UPDATE SKIP LOCKED
			""", nativeQuery=true)
	List<JobExecution> findReadyExecutionsForUpdate(
			@Param("limit") int limit);
	
	// For tests
	@Query(value = """
			SELECT *
			FROM job_executions
			WHERE status = 'READY'
			AND next_attempt_at <= :now
			ORDER BY next_attempt_at ASC
			LIMIT :limit
			FOR UPDATE SKIP LOCKED
			""", nativeQuery=true)
	List<JobExecution> findReadyExecutionsForUpdate(
			@Param("limit") int limit, 
			@Param("now") Instant now);
	
	@Modifying
	@Query("""
			UPDATE JobExecution e
			SET e.status = com.example.demo.execution.domain.ExecutionStatus.SUCCESS,
				e.completedAt = CURRENT_TIMESTAMP
			WHERE e.id = :executionId
			AND e.status = com.example.demo.execution.domain.ExecutionStatus.RUNNING
			AND e.workerId = :workerId
			AND e.leaseExpiresAt > :now
			""")
	int completeIfOwned(
			@Param("executionId") UUID executionId,
			@Param("workerId") String workerId,
			@Param("now") Instant now);
	
	
	@Modifying
	@Query(value = """
			UPDATE job_executions
			SET worker_id = :workerId,
			lease_expires_at = NOW() + INTERVAL '30 seconds'
			WHERE id = :executionId
			""", nativeQuery= true)
	int reassignForTest(
			@Param("executionId") UUID executionId,
			@Param("workerId") String workerId);
	
	@Modifying
	@Query("""
			UPDATE JobExecution e
			SET status = com.example.demo.execution.domain.ExecutionStatus.READY,
				e.workerId=NULL,
				e.leaseExpiresAt=NULL,
				e.startedAt=NULL
			WHERE e.status = com.example.demo.execution.domain.ExecutionStatus.RUNNING
				AND e.leaseExpiresAt <= :now
			""")
	int recoverExpiredLeases(
			@Param("now") Instant now);
	
	@Modifying
	@Query(value = """
			UPDATE job_executions
			SET lease_expires_at = :newLeaseExpiresAt 
			WHERE id = :executionId
				AND status = 'RUNNING'
				AND worker_id = :workerId
				AND lease_expires_at > :now
			""", nativeQuery = true)
	int renewLease(
			@Param("executionId") UUID executionId,
			@Param("workerId") String workerId,
			@Param("newLeaseExpiresAt") Instant newLeaseExpiresAt,
			@Param("now") Instant now);
	
	
	@Query( """
			SELECT e
			FROM JobExecution e 
			WHERE e.status = :status
			AND e.workerId = :workerId
			""" )
	List<JobExecution> findRunningExections(
			@Param("status") ExecutionStatus status,
			@Param("workerId") String workerId);
	
	
	long countByStatus(ExecutionStatus status);
	
	// Testing case
	Page<JobExecution> findAll(Pageable pageable);
	
	
	
	
	
	
	
	
	
}
