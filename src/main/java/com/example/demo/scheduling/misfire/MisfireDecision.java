package com.example.demo.scheduling.misfire;

import java.time.Instant;
import java.util.List;

public record MisfireDecision(
		List<Instant> executions,
		Instant nextRunAt) {

}
