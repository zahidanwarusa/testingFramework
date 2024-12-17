package com.umr.apitesting.reporting;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class ExtentReportManager {
	private static ExtentReports extent;
	private static ExtentTest test;
	private static ExtentTest currentKeyword;
	private static String reportPath;
	private static final Map<String, ExtentTest> testMap = new HashMap<>();

	public static void initializeReport() {
		try {
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
			reportPath = "reports/TestReport_" + timestamp + ".html";

			ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

			// Configure report appearance
			sparkReporter.config().setTheme(Theme.STANDARD);
			sparkReporter.config().setDocumentTitle("API Automation Test Report");
			sparkReporter.config().setReportName("API Test Execution Results");
			sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
			sparkReporter.config().setCss(".badge-primary { background-color: #116466 }");
			sparkReporter.config().setJs("document.getElementsByClassName('logo')[0].style.display='none';");

			extent = new ExtentReports();
			extent.attachReporter(sparkReporter);

			// Add system/environment info
			extent.setSystemInfo("Environment", ConfigManager.getCurrentEnvironment());
			extent.setSystemInfo("Base URL", ConfigManager.getProperty("base.url"));
			extent.setSystemInfo("Java Version", System.getProperty("java.version"));
			extent.setSystemInfo("OS", System.getProperty("os.name"));
			extent.setSystemInfo("User", System.getProperty("user.name"));

			new File("reports").mkdirs();
			LoggerUtil.logInfo("Initialized ExtentReports at: " + reportPath);

		} catch (Exception e) {
			LoggerUtil.logError("Failed to initialize ExtentReports", e);
		}
	}

	public static void startTest(String testId, String testName, String description) {
		test = extent.createTest(testId + " - " + testName);
		test.info(MarkupHelper.createLabel("Test Description: " + description, ExtentColor.BLUE));
		testMap.put(testId, test);
	}

	public static void startKeyword(String keyword) {
		if (test != null) {
			currentKeyword = test.createNode(keyword);
			currentKeyword.info(MarkupHelper.createLabel("Executing Keyword: " + keyword, ExtentColor.GREY));
		}
	}

	public static void logRequest(String url, String method, Map<String, String> headers, String body) {
		if (currentKeyword != null) {
			currentKeyword.info("<b>API Request Details:</b>");
			currentKeyword.info("URL: " + url);
			currentKeyword.info("Method: " + method);

			// Log headers in table format
			if (headers != null && !headers.isEmpty()) {
				String[][] headersArray = headers.entrySet().stream()
						.map(e -> new String[] { e.getKey(), e.getValue() }).toArray(String[][]::new);
				currentKeyword.info(MarkupHelper.createTable(headersArray));
			}

			// Log request body if present
			if (body != null && !body.trim().isEmpty()) {
				currentKeyword.info("Request Body:");
				currentKeyword.info(MarkupHelper.createCodeBlock(body, CodeLanguage.JSON));
			}
		}
	}

	public static void logResponse(Response response) {
		if (currentKeyword != null && response != null) {
			currentKeyword.info("<b>API Response Details:</b>");
			currentKeyword.info("Status Code: " + response.getStatusCode());
			currentKeyword.info("Response Time: " + response.getTime() + "ms");

			// Log response headers
			currentKeyword.info("Response Headers:");
			String[][] headersArray = response.headers().asList().stream()
					.map(h -> new String[] { h.getName(), h.getValue() }).toArray(String[][]::new);
			currentKeyword.info(MarkupHelper.createTable(headersArray));

			// Log response body
			String responseBody = response.getBody().asPrettyString();
			if (!responseBody.isEmpty()) {
				currentKeyword.info("Response Body:");
				currentKeyword.info(MarkupHelper.createCodeBlock(responseBody, CodeLanguage.JSON));
			}
		}
	}

	public static void logValidation(String validationType, String expected, String actual, boolean passed) {
		if (currentKeyword != null) {
			String[][] validationData = new String[][] { { "Validation Type", validationType },
					{ "Expected Value", expected }, { "Actual Value", actual },
					{ "Result", passed ? "PASS" : "FAIL" } };

			if (passed) {
				currentKeyword.pass(MarkupHelper.createLabel("Validation Passed", ExtentColor.GREEN));
			} else {
				currentKeyword.fail(MarkupHelper.createLabel("Validation Failed", ExtentColor.RED));
			}

			currentKeyword.info(MarkupHelper.createTable(validationData));
		}
	}

	public static void logKeywordResult(boolean passed, String message) {
		if (currentKeyword != null) {
			if (passed) {
				currentKeyword
						.pass(MarkupHelper.createLabel("Keyword Execution Successful: " + message, ExtentColor.GREEN));
			} else {
				currentKeyword.fail(MarkupHelper.createLabel("Keyword Execution Failed: " + message, ExtentColor.RED));
			}
		}
	}

	public static void logError(String message, Throwable e) {
		if (currentKeyword != null) {
			currentKeyword.fail(MarkupHelper.createLabel("Error: " + message, ExtentColor.RED));
			currentKeyword.fail(e);
		}
	}

	public static void endTest() {
		test = null;
		currentKeyword = null;
	}

	public static void finishReport() {
		if (extent != null) {
			extent.flush();
			LoggerUtil.logInfo("Detailed test report generated: " + reportPath);
		}
	}

	public static String getReportPath() {
		// TODO Auto-generated method stub
		return reportPath;
	}
}