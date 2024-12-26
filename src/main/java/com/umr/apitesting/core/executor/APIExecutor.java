package com.umr.apitesting.core.executor;

import com.umr.apitesting.core.context.TestContext;
import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.security.AuthManager;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class APIExecutor {
	private final TestContext context;

	public APIExecutor(TestContext context) {
		this.context = context;
	}

	public Response executeAPI(String baseUrl, String endpoint, String method, Object body) {
		try {
			RestClientBuilder builder = new RestClientBuilder().setBaseUrl(baseUrl).setEndpoint(endpoint)
					.setMethod(method).setRequestBody(body);

			// In executeAPI method
//			RestClientBuilder builder = new RestClientBuilder()
//			    .setBaseUrl(ConfigManager.getProperty("base.url"))
//			    .setEndpoint(endpoint)
//			    .setMethod(method)
//			    .setRequestBody(body);

			// Add auth token if required and available
			if (context.getTestData("AuthRequired") != null
					&& "Y".equalsIgnoreCase(context.getTestData("AuthRequired").toString())) {
				String token = AuthManager.getToken();
				builder.addHeader("Authorization", "Bearer " + token);
			}

			// Add any custom headers from test data
			if (context.getTestData("Headers") != null) {
				String[] headers = context.getTestData("Headers").toString().split(",");
				for (String header : headers) {
					String[] parts = header.split(":");
					if (parts.length == 2) {
						builder.addHeader(parts[0].trim(), parts[1].trim());
					}
				}
			}

			Response response = builder.execute();
			context.setResponse(response);
			return response;

		} catch (Exception e) {
			LoggerUtil.logError("API execution failed", e);
			throw new RuntimeException("API execution failed", e);
		}
	}

	public Object extractValue(String jsonPath) {
		Response response = context.getResponse();
		if (response == null) {
			throw new RuntimeException("No response available");
		}

		String responseBody = response.getBody().asString();
		LoggerUtil.logInfo("Response Body: " + responseBody);

		// Remove $ prefix if present
		String cleanPath = jsonPath.startsWith("$.") ? jsonPath.substring(2) : jsonPath;
		Object value = response.path(cleanPath);
		LoggerUtil.logInfo("Extracted value for path " + cleanPath + ": " + value);

		return value;
	}
}