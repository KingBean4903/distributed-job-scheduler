package com.example.demo.execution.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.execution.domain.JobExecution;

public interface JobExectionRepository extends JpaRepository<JobExecution, UUID> {}
