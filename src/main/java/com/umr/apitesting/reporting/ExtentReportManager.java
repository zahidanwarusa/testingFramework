package com.umr.apitesting.reporting;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.umr.apitesting.utils.LoggerUtil;

public class ExtentReportManager {
	private static ExtentReports extent;
	private static ExtentTest test;
	private static String reportPath;

	public static void initializeReport() {
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			reportPath = "reports/TestReport_" + timestamp + ".html";

			ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
			sparkReporter.config().setDocumentTitle("API Test Execution Report");
			sparkReporter.config().setReportName("Automated API Test Results");

			extent = new ExtentReports();
			extent.attachReporter(sparkReporter);
			extent.setSystemInfo("Environment", "Automation");
			extent.setSystemInfo("User", System.getProperty("user.name"));

			new File("reports").mkdirs();

			LoggerUtil.logInfo("Initialized ExtentReports at: " + reportPath);
		} catch (Exception e) {
			LoggerUtil.logError("Failed to initialize ExtentReports", e);
		}
	}

	public static void startTest(String testId, String testName) {
		test = extent.createTest(testId + " - " + testName);
	}

	public static void logInfo(String message) {
		if (test != null) {
			test.log(Status.INFO, message);
		}
	}

	public static void logPass(String message) {
		if (test != null) {
			test.log(Status.PASS, message);
		}
	}

	public static void logFail(String message, Throwable e) {
		if (test != null) {
			test.log(Status.FAIL, message);
			test.log(Status.FAIL, e);
		}
	}

	public static void finishReport() {
		if (extent != null) {
			extent.flush();
			LoggerUtil.logInfo("Report generated: " + reportPath);
		}
	}
}