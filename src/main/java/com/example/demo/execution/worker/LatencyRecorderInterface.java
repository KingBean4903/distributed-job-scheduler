package com.example.demo.execution.worker;

public interface LatencyRecorderInterface {
	
	long count();
	void record(long percentile);
	double percentile(double percentile);
	double medianMs();
	double p95Ms();
}
