package com.umr.apitesting.web.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionProgress {
	private String testId;
	private String status;
	private List<String> logs;
	private long startTime;
	private long endTime;
	private int completedSteps;
	private int totalSteps;
}