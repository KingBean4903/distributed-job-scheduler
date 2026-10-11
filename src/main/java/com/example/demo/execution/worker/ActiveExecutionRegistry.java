package com.example.demo.execution.worker;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ActiveExecutionRegistry {

	private final Set<UUID> executions = 
			ConcurrentHashMap.newKeySet();
	
	public void add(UUID executionId) {
		executions.add(executionId);
	}
	
	public void remove(UUID executionId) {
		executions.remove(executionId);
	}
	
	public Set<UUID> snapshot() {
		return Set.copyOf(executions);
	}
}
