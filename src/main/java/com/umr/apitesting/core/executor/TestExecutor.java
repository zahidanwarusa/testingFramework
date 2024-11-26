package com.umr.apitesting.core.executor;

import java.util.List;
import java.util.Map;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.utils.ExcelReader;
import com.umr.apitesting.utils.LoggerUtil;

public class TestExecutor {
	private final KeywordExecutor keywordExecutor;
	private final String testDataPath;

	public TestExecutor(String testDataPath) {
		this.keywordExecutor = new KeywordExecutor();
		this.testDataPath = testDataPath;
	}

	public void executeTest(String testId) {
		try {
			// Create test context
			TestContext context = new TestContext(testId);

			// Load test data
			loadTestData(context);

			// Get keywords for the test
			List<String> keywords = getKeywords(testId);

			LoggerUtil.logInfo("Executing test: " + testId);
			LoggerUtil.logInfo("Test data: " + context.getTestData("BaseURL") + context.getTestData("Endpoint"));

			// Execute each keyword
			for (String keyword : keywords) {
				LoggerUtil.logInfo("Executing keyword: " + keyword);
				boolean result = keywordExecutor.executeKeyword(keyword, context);
				if (!result) {
					LoggerUtil.logError("Keyword execution failed: " + keyword, null);
					throw new RuntimeException("Test failed at keyword: " + keyword);
				}
			}

		} catch (Exception e) {
			LoggerUtil.logError("Test execution failed: " + testId, e);
			throw new RuntimeException("Test execution failed", e);
		}
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

		return reader.readData().stream().filter(row -> row.get("TestID").equals(testId)).map(row -> row.get("Keyword"))
				.toList();
	}
}