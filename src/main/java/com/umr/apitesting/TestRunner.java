package com.umr.apitesting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.core.executor.ParallelTestExecutor;
import com.umr.apitesting.reporting.ExtentReportManager;
import com.umr.apitesting.security.AuthManager;
import com.umr.apitesting.utils.ExcelReader;
import com.umr.apitesting.utils.LoggerUtil;

public class TestRunner {
	private static final String TEST_SUITE_PATH = "src/main/resources/testdata/TestSuite.xlsx";
	private static final int DEFAULT_THREAD_COUNT = 5;

	public static void main(String[] args) {
		try {
			// Load environment and initialize auth
			String env = System.getProperty("env", "dev");
			ConfigManager.loadEnvironmentConfig(env);
			AuthManager.loadAuthConfig(ConfigManager.CONFIG_PATH + env + ".properties");

			// Initialize reporting
			ExtentReportManager.initializeReport();
			LoggerUtil.logInfo("Starting test execution in " + env + " environment");
			ExtentReportManager.startTest("Setup", "Environment Setup",
					"Initializing test execution in " + env + " environment");

			// Read and filter test cases
			ExcelReader reader = new ExcelReader(TEST_SUITE_PATH);
			reader.setSheet("TestCases");
			List<Map<String, String>> allTests = reader.readData();
			List<Map<String, String>> testsToExecute = allTests.stream()
					.filter(test -> "Y".equalsIgnoreCase(test.get("Execute"))).collect(Collectors.toList());

			LoggerUtil.logInfo("Found " + testsToExecute.size() + " test cases to execute");

			// Execute tests in parallel
			int threadCount = Integer.parseInt(System.getProperty("threads", String.valueOf(DEFAULT_THREAD_COUNT)));
			ParallelTestExecutor executor = new ParallelTestExecutor(TEST_SUITE_PATH, threadCount);
			executor.executeTests(testsToExecute);

			// Print summary
			int passed = executor.getPassedCount();
			int failed = executor.getFailedCount();
			int total = testsToExecute.size();

			LoggerUtil.logInfo("\n=== Test Execution Summary ===");
			LoggerUtil.logInfo("Total Tests: " + total);
			LoggerUtil.logInfo("Passed: " + passed);
			LoggerUtil.logInfo("Failed: " + failed);
			LoggerUtil.logInfo("Success Rate: " + String.format("%.2f%%", (passed * 100.0 / total)));

			ExtentReportManager.endTest();
			ExtentReportManager.finishReport();

		} catch (Exception e) {
			LoggerUtil.logError("Test execution failed", e);
			ExtentReportManager.logError("Test execution failed", e);
			System.exit(1);
		}
	}
}