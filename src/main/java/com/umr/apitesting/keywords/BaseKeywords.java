package com.umr.apitesting.keywords;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class BaseKeywords {

	@Keyword(value = "GET", description = "Executes a GET request")
	public boolean executeGet(TestContext context) {
		try {
			String baseUrl = (String) context.getTestData("BaseURL");
			String endpoint = (String) context.getTestData("Endpoint");
			String headers = (String) context.getTestData("Headers");

			RestClientBuilder builder = new RestClientBuilder().setBaseUrl(baseUrl).setEndpoint(endpoint)
					.setMethod("GET");

			// Add headers if present
			if (headers != null && !headers.isEmpty()) {
				String[] headerPairs = headers.split(",");
				for (String pair : headerPairs) {
					String[] keyValue = pair.split(":");
					builder.addHeader(keyValue[0].trim(), keyValue[1].trim());
				}
			}

			// Execute request and log response
			Response response = builder.execute();
			String responseBody = response.getBody().asString();
			LoggerUtil.logInfo("Response Status Code: " + response.getStatusCode());
			LoggerUtil.logInfo("Response Body: " + responseBody);

			// Store response in context
			context.setResponse(response);

			return true;
		} catch (Exception e) {
			LoggerUtil.logError("GET request failed", e);
			return false;
		}
	}

	@Keyword(value = "POST", description = "Executes a POST request")
	public boolean executePost(TestContext context) {
		try {
			Response response = new RestClientBuilder().setBaseUrl((String) context.getTestData("baseUrl"))
					.setEndpoint((String) context.getTestData("endpoint")).setMethod("POST")
					.setRequestBody(context.getTestData("requestBody")).execute();

			context.setResponse(response);
			return response.getStatusCode() == 201;
		} catch (Exception e) {
			LoggerUtil.logError("POST request failed", e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_STATUS")
	public boolean verifyStatus(TestContext context) {
		try {
			Response response = context.getResponse();
			Object expectedStatusObj = context.getTestData("ExpectedStatus");

			if (expectedStatusObj == null) {
				LoggerUtil.logError("ExpectedStatus is null in test data", null);
				return false;
			}

			// Handle different numeric formats (integer or decimal)
			int expectedStatus;
			if (expectedStatusObj instanceof Double) {
				expectedStatus = ((Double) expectedStatusObj).intValue();
			} else {
				expectedStatus = Integer.parseInt(expectedStatusObj.toString().split("\\.")[0]);
			}

			boolean result = response.getStatusCode() == expectedStatus;

			LoggerUtil.logInfo(String.format("Status code validation: Expected=%d, Actual=%d", expectedStatus,
					response.getStatusCode()));

			return result;
		} catch (Exception e) {
			LoggerUtil.logError("Status verification failed: " + e.getMessage(), e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_RESPONSE", description = "Verifies response contains expected data")
	public boolean verifyResponse(TestContext context) {
		Response response = context.getResponse();
		String expectedData = (String) context.getTestData("expectedData");
		return response.getBody().asString().contains(expectedData);
	}

	@Keyword(value = "VERIFY_JSON_PATH")
	public boolean verifyJsonPath(TestContext context) {
		try {
			Response response = context.getResponse();
			String jsonPath = (String) context.getTestData("ValidationPath");
			String expectedValue = (String) context.getTestData("ExpectedValue");

			// Log the parameters for debugging
			LoggerUtil.logInfo("Validating JSON Path:");
			LoggerUtil.logInfo("Path: " + jsonPath);
			LoggerUtil.logInfo("Expected Value: " + expectedValue);

			String responseBody = response.getBody().asString();
			LoggerUtil.logInfo("Response Body: " + responseBody);

			// Use JsonPath from io.restassured.path.json
			io.restassured.path.json.JsonPath jsonPathEvaluator = response.jsonPath();

			// Handle nested paths properly
			String processedPath = processJsonPath(jsonPath);
			Object actualValueObj = jsonPathEvaluator.get(processedPath);

			if (actualValueObj == null) {
				LoggerUtil.logError("No value found for JsonPath: " + jsonPath, null);
				return false;
			}

			String actualValue = actualValueObj.toString();
			LoggerUtil.logInfo("Actual value found: " + actualValue);

			// Compare values using contains for more flexible matching
			boolean result = actualValue.contains(expectedValue);

			if (result) {
				LoggerUtil.logInfo("JSON path validation PASSED");
				LoggerUtil.logInfo(String.format("Verified that '%s' contains '%s'", actualValue, expectedValue));
			} else {
				LoggerUtil.logInfo("JSON path validation FAILED");
				LoggerUtil.logInfo(String.format("Expected '%s' to contain '%s'", actualValue, expectedValue));
			}

			return result;

		} catch (Exception e) {
			LoggerUtil.logError("JSON path verification failed: " + e.getMessage(), e);
			LoggerUtil.logError("Stack trace: ", e);
			return false;
		}
	}

	/**
	 * Processes the JSON path to ensure proper format for different notations
	 */
	private String processJsonPath(String jsonPath) {
		if (jsonPath == null || jsonPath.trim().isEmpty()) {
			throw new IllegalArgumentException("JSON path cannot be null or empty");
		}

		// Remove leading "$." if present
		jsonPath = jsonPath.replaceFirst("^\\$\\.", "");

		// Handle different notation styles
		if (jsonPath.contains("[")) {
			// Array notation is already correct, return as is
			return jsonPath;
		} else {
			// Handle dot notation
			String[] pathParts = jsonPath.split("\\.");
			StringBuilder processedPath = new StringBuilder();

			for (int i = 0; i < pathParts.length; i++) {
				if (i > 0) {
					processedPath.append(".");
				}
				// Check if the part represents an array index
				if (pathParts[i].matches("\\d+")) {
					processedPath.append("[").append(pathParts[i]).append("]");
				} else {
					processedPath.append(pathParts[i]);
				}
			}

			return processedPath.toString();
		}
	}
}
