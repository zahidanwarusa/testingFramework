package com.umr.apitesting.core.executor;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.keywords.Keyword;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class ApiKeywords {

	@Keyword(value = "GET")
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

			Response response = builder.execute();
			context.setResponse(response);
			return true;
		} catch (Exception e) {
			LoggerUtil.logError("GET request failed", e);
			return false;
		}
	}

	@Keyword(value = "POST")
	public boolean executePost(TestContext context) {
		try {
			String baseUrl = (String) context.getTestData("BaseURL");
			String endpoint = (String) context.getTestData("Endpoint");
			String headers = (String) context.getTestData("Headers");
			String requestBody = (String) context.getTestData("RequestBody");

			RestClientBuilder builder = new RestClientBuilder().setBaseUrl(baseUrl).setEndpoint(endpoint)
					.setMethod("POST").setRequestBody(requestBody);

			// Add headers if present
			if (headers != null && !headers.isEmpty()) {
				String[] headerPairs = headers.split(",");
				for (String pair : headerPairs) {
					String[] keyValue = pair.split(":");
					builder.addHeader(keyValue[0].trim(), keyValue[1].trim());
				}
			}

			Response response = builder.execute();
			context.setResponse(response);
			return true;
		} catch (Exception e) {
			LoggerUtil.logError("POST request failed", e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_STATUS")
	public boolean verifyStatus(TestContext context) {
		String contextData = context.getTestData("ExpectedStatus").toString();
		LoggerUtil.logInfo("Conteext Data: " + contextData);
		try {
			Response response = context.getResponse();

			int expectedStatus = Integer.parseInt(context.getTestData("ExpectedStatus").toString());
			boolean result = response.getStatusCode() == expectedStatus;

			LoggerUtil.logInfo(String.format("Status code validation: Expected=%d, Actual=%d", expectedStatus,
					response.getStatusCode()));

			return result;
		} catch (Exception e) {
			LoggerUtil.logError("Status verification failed", e);
			return false;
		}
	}

	@Keyword(value = "VERIFY_JSON_PATH")
	public boolean verifyJsonPath(TestContext context) {
		try {
			Response response = context.getResponse();
			String jsonPath = (String) context.getTestData("ValidationPath");
			String expectedValue = (String) context.getTestData("ExpectedValue");

			String actualValue = JsonPath.from(response.getBody().asString()).getString(jsonPath);
			boolean result = expectedValue.equals(actualValue);

			LoggerUtil.logInfo(String.format("JSON path validation: Path=%s, Expected=%s, Actual=%s", jsonPath,
					expectedValue, actualValue));

			return result;
		} catch (Exception e) {
			LoggerUtil.logError("JSON path verification failed", e);
			return false;
		}
	}
}