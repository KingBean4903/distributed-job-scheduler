package com.example.demo.scheduler.execution.executor.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class MutableClock extends Clock {

	private Instant current;
	
	public MutableClock(Instant current) {
		this.current = current;
	}
	
	public void advance(Duration duration) {
		current = current.plus(duration);
	}
	
	public void set(Instant instant) {
		current = instant;
	}
	
	@Override
	public ZoneId getZone() {
		return ZoneOffset.UTC;
	}

	@Override
	public Instant instant() {
		return current;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return this;
	}

}
