package com.example.demo.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface JobRepository extends JpaRepository<Job, UUID> {
	
	List<Job> findTop100ByStatusAndNextRunAtLessThanOrderByNextRunAtAsc(JobStatus status, Instant now);
	
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(""" 
			SELECT j
			FROM Job j
			WHERE j.id = :id
	""")
	java.util.Optional<Job> findByIdForUpdate(@Param("id") UUID id);
	
	@Query("""
			SELECT j
			FROM Job j
			WHERE j.status = :status
			AND j.nextRunAt <= :now
			ORDER BY j.nextRunAt ASC
			""")
	List<Job> findDueJobs(@Param("status") JobStatus status,
			@Param("now") Instant now);
	
	
	@Query(
			value = """
					 SELECT * 
					 FROM jobs
					 WHERE status = 'ACTIVE'
					 AND next_run_at <= NOW()
					 ORDER BY next_run_at
					 LIMIT :limit
					 FOR UPDATE SKIP LOCKED
					""",
					nativeQuery = true)
	List<Job> findDueJobsForUpdate(@Param("limit") int limit);
}
