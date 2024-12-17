package com.umr.apitesting.web.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestExecutionStatus {
	public enum Status {
		NOT_STARTED, RUNNING, COMPLETED, FAILED, ERROR
	}

	private String testId;
	private Status status;
	private String message;
	private long startTime;
	private long endTime;
	private int completedSteps;
	private int totalSteps;
}