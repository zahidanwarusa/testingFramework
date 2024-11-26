package com.umr.apitesting;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.core.executor.ParallelTestExecutor;
import com.umr.apitesting.reporting.ExtentReportManager;
import com.umr.apitesting.utils.ExcelReader;
import com.umr.apitesting.utils.LoggerUtil;

public class TestRunner {
    private static final String TEST_SUITE_PATH = "src/main/resources/testdata/TestSuite.xlsx";
    private static final int DEFAULT_THREAD_COUNT = 5;

    public static void main(String[] args) {
        try {
            // Initialize reporting
            ExtentReportManager.initializeReport();

            // Initialize configuration
            String env = System.getProperty("env", "dev");
            ConfigManager.loadEnvironmentConfig(env);
            
            LoggerUtil.logInfo("Starting test execution in " + env + " environment");
            ExtentReportManager.logInfo("Test execution started in " + env + " environment");

            // Read test cases
            ExcelReader reader = new ExcelReader(TEST_SUITE_PATH);
            reader.setSheet("TestCases");
            List<Map<String, String>> allTests = reader.readData();
            
            // Filter tests marked for execution
            List<Map<String, String>> testsToExecute = allTests.stream()
                .filter(test -> "Y".equalsIgnoreCase(test.get("Execute")))
                .collect(Collectors.toList());

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

            // Finish reporting
            ExtentReportManager.finishReport();

        } catch (Exception e) {
            LoggerUtil.logError("Test execution failed", e);
            ExtentReportManager.logFail("Test execution failed", e);
            System.exit(1);
        }
    }
}