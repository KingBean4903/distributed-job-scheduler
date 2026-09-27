package com.example.demo.execution.worker;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class WorkerIdentity {

	private final String workerId = 
			UUID.randomUUID().toString();
	
	public String getWorkerId() {
		return workerId;
	}
}
