package com.example.demo.execution.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.execution.domain.ExecutionStatus;
import com.example.demo.execution.domain.JobExecution;

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
	
	
	@Modifying
	@Query(value = """
			UPDATE job_exeuctions
			SET status = 'SUCCESS',
				completed_at = NOW()
			WHERE id = :executionId
			AND status = 'RUNNING'
			AND worker_id = :workerId
			AND lease_expires_at > NOW() 
			""", nativeQuery = true)
	int completeIfOwned(
			@Param("executionId") UUID executionId,
			@Param("workerId") String workerId);
	
	
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
	@Query(value = """
			UPDATE job_executions
			SET status = 'READY',
				worker_id=NULL,
				lease_expires_at=NULL,
				started_at=NULL
			WHERE status = 'RUNNING'
				AND lease_expires_at <= NOW()
			""", nativeQuery = true)
	int recoverExpiredLeases();
	
	@Modifying
	@Query(value = """
			UPDATE job_executions
			SET lease_expires_at = NOW() + INTERVAL '30 seconds'
			WHERE id = :executionId
				AND status = 'RUNNING'
				AND worker_id = :workerId
				AND lease_expires_at > NOW()
			""", nativeQuery = true)
	int renewLease(
			@Param("executionId") UUID executionId,
			@Param("workerId") String workerId);
	
	
	@Query( """
			SELECT e
			FROM JobExecution e 
			WHERE e.status = :status
			AND e.workerId = :workerId
			""" )
	List<JobExecution> findRunningExections(
			@Param("status") ExecutionStatus status,
			@Param("workerId") String workerId);
	
	
	
	
	
	
	
	
	
}
