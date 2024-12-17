package com.umr.apitesting.web.model;

import lombok.Data;

@Data
public class TestCase {
	private String testId;
	private String testName;
	private String description;
	private String execute;
	private String priority;
	private String authRequired;
}
