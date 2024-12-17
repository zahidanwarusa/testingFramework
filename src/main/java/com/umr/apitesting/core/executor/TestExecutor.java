package com.umr.apitesting.core.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.utils.ExcelReader;
import com.umr.apitesting.utils.LoggerUtil;
import com.umr.apitesting.web.model.TestExecutionStatus;
import com.umr.apitesting.web.model.TestExecutionStatus.Status;

@Component
public class TestExecutor {
	private final KeywordExecutor keywordExecutor;
	private final String testDataPath;
	private final Map<String, TestExecutionStatus> testStatuses;

	public TestExecutor(String testDataPath) {
		this.keywordExecutor = new KeywordExecutor();
		this.testDataPath = testDataPath;
		this.testStatuses = new ConcurrentHashMap<>();
	}

//	public void executeTest(String testId) {
//		try {
//			
//			// Create test context
//			TestContext context = new TestContext(testId);
//
//			// Load test data
//			loadTestData(context);
//
//			// Get keywords for the test
//			List<String> keywords = getKeywords(testId);
//
//			LoggerUtil.logInfo("Executing test: " + testId);
//			LoggerUtil.logInfo("Test data: " + context.getTestData("BaseURL") + context.getTestData("Endpoint"));
//
//			// Execute each keyword
//			for (String keyword : keywords) {
//				LoggerUtil.logInfo("Executing keyword: " + keyword);
//				boolean result = keywordExecutor.executeKeyword(keyword, context);
//				if (!result) {
//					LoggerUtil.logError("Keyword execution failed: " + keyword, null);
//					throw new RuntimeException("Test failed at keyword: " + keyword);
//				}
//			}
//
//		} catch (Exception e) {
//			LoggerUtil.logError("Test execution failed: " + testId, e);
//			throw new RuntimeException("Test execution failed", e);
//		}
//	}

	public void executeTest(String testId) {
		try {
			// Initialize test status
			TestExecutionStatus status = TestExecutionStatus.builder().testId(testId).status(Status.RUNNING)
					.startTime(System.currentTimeMillis()).build();
			testStatuses.put(testId, status);

			// Create test context
			TestContext context = new TestContext(testId);

			// Load test data
			loadTestData(context);

			// Get keywords for the test
			List<String> keywords = getKeywords(testId);

			status.setTotalSteps(keywords.size());
			updateTestStatus(status);

			LoggerUtil.logInfo("Executing test: " + testId);
			LoggerUtil.logInfo("Test data: " + context.getTestData("BaseURL") + context.getTestData("Endpoint"));

			// Execute each keyword
			for (String keyword : keywords) {
				LoggerUtil.logInfo("Executing keyword: " + keyword);
				boolean result = keywordExecutor.executeKeyword(keyword, context);
				if (!result) {
					status.setStatus(Status.FAILED);
					status.setMessage("Keyword execution failed: " + keyword);
					updateTestStatus(status);
					LoggerUtil.logError("Keyword execution failed: " + keyword, null);
					throw new RuntimeException("Test failed at keyword: " + keyword);
				}
				status.setCompletedSteps(status.getCompletedSteps() + 1);
				updateTestStatus(status);
			}

			// Update final status
			status.setStatus(Status.COMPLETED);
			status.setEndTime(System.currentTimeMillis());
			updateTestStatus(status);

		} catch (Exception e) {
			TestExecutionStatus status = testStatuses.get(testId);
			if (status != null) {
				status.setStatus(Status.ERROR);
				status.setMessage(e.getMessage());
				status.setEndTime(System.currentTimeMillis());
				updateTestStatus(status);
			}
			LoggerUtil.logError("Test execution failed: " + testId, e);
			throw new RuntimeException("Test execution failed", e);
		}
	}

	public TestExecutionStatus getTestStatus(String testId) {
		return testStatuses.getOrDefault(testId,
				TestExecutionStatus.builder().testId(testId).status(Status.NOT_STARTED).build());
	}

	private void updateTestStatus(TestExecutionStatus status) {
		testStatuses.put(status.getTestId(), status);
	}

	private void loadTestData(TestContext context) {
		ExcelReader reader = new ExcelReader(testDataPath);
		reader.setSheet("TestData");

		List<Map<String, String>> testData = reader.readData();
		Map<String, String> data = testData.stream().filter(row -> row.get("TestID").equals(context.getTestId()))
				.findFirst().orElseThrow(() -> new RuntimeException("Test data not found: " + context.getTestId()));

		data.forEach(context::setTestData);

		LoggerUtil.logInfo("Test Data: " + data);
		String contextData = context.getTestData("ExpectedStatus").toString();
		LoggerUtil.logInfo("Conteext Data: " + contextData);
	}

	private List<String> getKeywords(String testId) {
		ExcelReader reader = new ExcelReader(testDataPath);
		reader.setSheet("Keywords");

		List<Map<String, String>> data = reader.readData();
		Map<String, String> testRow = data.stream().filter(row -> testId.equals(row.get("TestID"))).findFirst()
				.orElseThrow(() -> new RuntimeException("Keywords not found for test: " + testId));

		// Create ordered list of keywords
		List<String> keywords = new ArrayList<>();
		for (int i = 1;; i++) {
			String key = "Keyword" + i;
			String keyword = testRow.get(key);
			if (keyword == null || keyword.trim().isEmpty()) {
				break;
			}
			keywords.add(keyword.trim());
		}

		LoggerUtil.logInfo("Keywords for test " + testId + ": " + keywords);
		return keywords;
	}
}