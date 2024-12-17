package com.umr.apitesting.web.model;

import java.util.List;

import lombok.Data;

@Data
public class ExecutionRequest {
	public enum ExecutionType {
		ALL, EXECUTABLE, FILTERED
	}

	private ExecutionType type;
	private List<String> keywords;
	private String serviceId;
	private String endpointId;
}