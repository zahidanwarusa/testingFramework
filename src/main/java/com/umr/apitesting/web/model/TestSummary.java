package com.umr.apitesting.web.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestSummary {
	private Map<String, Integer> testsByService;
	private int totalTests;
	private ExecutionStatus status;

	public enum ExecutionStatus {
		IN_PROGRESS, COMPLETED, FAILED, NOT_STARTED
	}
}