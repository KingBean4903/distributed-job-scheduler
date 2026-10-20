package com.example.demo.stats.calc;

import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StatsCalc {
	
	
	@Test
	void calcMedian() {
		
		Map<Double, String> lookups = new TreeMap();
		
		lookups.put(3.0, "ONE");
		lookups.put(2.0, "ONE");
		lookups.put(31.0, "ONE");
		lookups.put(5.0, "ONE");
		
		List<Double> lItems = lookups.keySet().stream().collect(Collectors.toList());
		
		assertThat(lItems).containsExactly(2.0, 3.0, 5.0, 31.0);
		
		if (lItems.size()%2 == 0) {
			int midIdx =  (lItems.size()/2) - 1;
			
			double midV = lItems.get(midIdx);
			double midV2 = lItems.get(midIdx + 1);
			
			assertThat(midV).isEqualTo(3.0);
			assertThat(midV2).isEqualTo(5.0);
			
			double median = (midV + midV2) / 2;
			
			assertThat(median).isEqualTo(4.0);
			
		}
		
		
		
	}

}
