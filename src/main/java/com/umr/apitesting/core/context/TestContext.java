package com.umr.apitesting.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.restassured.response.Response;

public class TestContext {
	private final String testId;
	private final Map<String, Object> testData;
	private final Map<String, Object> runtimeData;
	private Response apiResponse;
	private final AtomicInteger retryCount;
	private final Map<String, Object> sharedData;

	public TestContext(String testId) {
		this.testId = testId;
		this.testData = new HashMap<>();
		this.runtimeData = new HashMap<>();
		this.sharedData = new ConcurrentHashMap<>();
		this.retryCount = new AtomicInteger(0);
	}

	public String getTestId() {
		return testId;
	}

	public void setTestData(String key, Object value) {
		testData.put(key, value);
	}

	public Object getTestData(String key) {
		return testData.get(key);
	}

	public void setRuntimeData(String key, Object value) {
		runtimeData.put(key, value);
	}

	public Object getRuntimeData(String key) {
		return runtimeData.get(key);
	}

	public void setResponse(Response response) {
		this.apiResponse = response;
	}

	public Response getResponse() {
		return apiResponse;
	}

	public void incrementRetryCount() {
		retryCount.incrementAndGet();
	}

	public int getRetryCount() {
		return retryCount.get();
	}

	public void setSharedData(String key, Object value) {
		sharedData.put(key, value);
	}

	public Object getSharedData(String key) {
		return sharedData.get(key);
	}
}
