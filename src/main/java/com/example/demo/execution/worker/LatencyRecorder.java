package com.example.demo.execution.worker;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

@Component
public class LatencyRecorder {

    private final List<Long> samplesMicros =
            Collections.synchronizedList(new ArrayList<>());

    public void record(long durationNanos) {
        long durationMicros =
                TimeUnit.NANOSECONDS.toMicros(durationNanos);

        samplesMicros.add(durationMicros);
    }

    public long count() {
        return samplesMicros.size();
    }

    public double percentile(double percentile) {
        List<Long> sorted;

        synchronized (samplesMicros) {
            sorted = new ArrayList<>(samplesMicros);
        }

        if (sorted.isEmpty()) {
            return 0.0;
        }

        Collections.sort(sorted);

        int index = (int) Math.ceil(percentile * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index) / 1000.0;
    }

    public double medianMs() {
        return percentile(0.50);
    }

    public double p95Ms() {
        return percentile(0.95);
    }
}

