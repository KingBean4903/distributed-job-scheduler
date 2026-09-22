package com.example.demo.execution.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.execution.domain.JobExecution;

public interface JobExectionRepository extends JpaRepository<JobExecution, UUID> {
	
	
	@Query(value = """
			SELECT *
			FROM job_executions
			WHERE status = 'READY'
			ORDER BY scheduled_at ASC
			LIMIT :limit
			FOR UPDATE SKIP LOCKED
			""", nativeQuery=true)
	List<JobExecution> findReadyExecutionsForUpdate(
			@Param("limit") int limit);
}
