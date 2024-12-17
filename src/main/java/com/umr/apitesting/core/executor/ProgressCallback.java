package com.umr.apitesting.core.executor;

public interface ProgressCallback {
	void onProgress(String testId, String status, String message);
}