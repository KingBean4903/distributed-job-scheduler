package com.example.demo.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, UUID> {
	
	List<Job> findTop100ByStatusAndNextRunAtAsc(JobStatus status, Instant now);
}
