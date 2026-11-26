package com.example.demo.benchmark;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BenchmarkVerification {
	
	private final JdbcTemplate jdbcTemplate;
	
	public BenchmarkVerification(
			JdbcTemplate jdbcTemplate) {
		
		this.jdbcTemplate = jdbcTemplate;
	}
	
	
	public Map<String, Long> countByStatus() { 
		
		return jdbcTemplate.query(
				""" 
				SELECT status::text,
				COUNT(*)
				FROM job_executions
				GROUP BY status
				""", rs -> {
					
					Map<String, Long> result = 
							new TreeMap<>();
					
					while(rs.next()) {
						result.put(rs.getString(1), rs.getLong(2));
					}
					return result;
				});
	}
	
	public Long totalJobs() {
		Long count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM jobs", 
				Long.class);
		
		return count == null ? 0L : count;
	}
	
	public Long totalExecutions() {
		Long count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM job_executions", 
				Long.class);
		
		return count == null ? 0L : count;
	}
	
	
	public Long nonTerminalExecutions() {
		
		Long count = jdbcTemplate.queryForObject(
				"""
				SELECT COUNT(*) FROM job_executions
				WHERE status IN('READY', 'RUNNING')
				""", Long.class);
		
		return count == null ? 0L : count;
	}
		
}
