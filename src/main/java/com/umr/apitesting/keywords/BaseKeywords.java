package com.umr.apitesting.keywords;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.core.executor.APIExecutor;
import com.umr.apitesting.reporting.ExtentReportManager;
import com.umr.apitesting.utils.JsonUtils;
import com.umr.apitesting.utils.LoggerUtil;

public class BaseKeywords {

	@Keyword(value = "API_CALL", description = "Executes API request based on test data")
	public boolean executeAPI(TestContext context) {
		ExtentReportManager.startKeyword("API_CALL");
		try {

			String requestBodyJson = JsonUtils.makeJson((String) context.getTestData("RequestBodyKey"),
					(String) context.getTestData("RequestBodyValue"));
			APIExecutor executor = new APIExecutor(context);
			executor.executeAPI((String) context.getTestData("BaseURL"), (String) context.getTestData("Endpoint"),
					(String) context.getTestData("Method"), requestBodyJson);
			ExtentReportManager.logKeywordResult(true, "API call executed successfully");
			return true;
		} catch (Exception e) {
			LoggerUtil.logError("API call failed", e);
			ExtentReportManager.logError("API call failed", e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_RESPONSE")
	public boolean verifyResponse(TestContext context) {
		ExtentReportManager.startKeyword("VERIFY_RESPONSE");
		try {
			APIExecutor executor = new APIExecutor(context);
			String path = context.getTestData("ValidationPath").toString();
			String expected = context.getTestData("ExpectedValue").toString();

			Object actual = executor.extractValue(path);
//			boolean result = expected.equals(String.valueOf(actual));
			boolean result = String.valueOf(actual).contains(expected);

			ExtentReportManager.logValidation(path, expected, String.valueOf(actual), result);

			if (!result) {
				LoggerUtil.logInfo("Validation failed - Expected: " + expected + ", Actual: " + actual);
				return false;
			}

			ExtentReportManager.logKeywordResult(true, "Response validation successful");
			return true;
		} catch (Exception e) {
			LoggerUtil.logError("Response validation failed", e);
			ExtentReportManager.logError("Response validation failed", e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_STATUS")
	public boolean verifyStatus(TestContext context) {
		ExtentReportManager.startKeyword("VERIFY_STATUS");
		try {
			double expectedStatusDouble = Double.parseDouble(context.getTestData("ExpectedStatus").toString());
			int expectedStatus = (int) expectedStatusDouble;
			int actualStatus = context.getResponse().getStatusCode();
			boolean result = actualStatus == expectedStatus;

			ExtentReportManager.logValidation("Status Code", String.valueOf(expectedStatus),
					String.valueOf(actualStatus), result);

			return result;
		} catch (Exception e) {
			LoggerUtil.logError("Status verification failed", e);
			ExtentReportManager.logError("Status verification failed", e);
			return false;
		}
	}
}