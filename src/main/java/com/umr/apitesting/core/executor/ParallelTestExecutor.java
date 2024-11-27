package com.umr.apitesting.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.umr.apitesting.reporting.ExtentReportManager;
import com.umr.apitesting.utils.LoggerUtil;

public class ParallelTestExecutor {
	private final AtomicInteger passed = new AtomicInteger(0);
	private final AtomicInteger failed = new AtomicInteger(0);
	private final String testDataPath;
	private final int threadCount;

	public ParallelTestExecutor(String testDataPath, int threadCount) {
		this.testDataPath = testDataPath;
		this.threadCount = threadCount;
	}

	public void executeTests(List<Map<String, String>> testsToExecute) {
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		for (Map<String, String> test : testsToExecute) {
			executor.submit(() -> executeTest(test));
		}

		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			LoggerUtil.logError("Test execution interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	private void executeTest(Map<String, String> test) {
		String testId = test.get("TestID");
		String testName = test.get("TestName");
		String description = test.get("Description");

		try {
			ExtentReportManager.startTest(testId, testName, description != null ? description : "");
			LoggerUtil.logInfo("Executing test: " + testId + " - " + testName);

			TestExecutor executor = new TestExecutor(testDataPath);
			executor.executeTest(testId);

			passed.incrementAndGet();
			ExtentReportManager.logKeywordResult(true, "Test " + testId + " completed successfully");
			LoggerUtil.logInfo("Test " + testId + " PASSED");
		} catch (Exception e) {
			failed.incrementAndGet();
			ExtentReportManager.logError("Test " + testId + " failed", e);
			LoggerUtil.logError("Test " + testId + " FAILED", e);
		} finally {
			ExtentReportManager.endTest();
		}
	}

	public int getPassedCount() {
		return passed.get();
	}

	public int getFailedCount() {
		return failed.get();
	}
}