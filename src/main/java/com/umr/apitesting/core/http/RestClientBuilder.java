package com.umr.apitesting.core.http;

import java.util.HashMap;
import java.util.Map;

import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestClientBuilder {
	private String baseUrl;
	private String endpoint;
	private String method;
	private Map<String, String> headers;
	private Map<String, String> queryParams;
	private Object requestBody;

	public RestClientBuilder() {
		this.headers = new HashMap<>();
		this.queryParams = new HashMap<>();
	}

	public RestClientBuilder setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
		return this;
	}

	public RestClientBuilder setEndpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	public RestClientBuilder setMethod(String method) {
		this.method = method;
		return this;
	}

	public RestClientBuilder addHeader(String key, String value) {
		headers.put(key, value);
		return this;
	}

	public RestClientBuilder addQueryParam(String key, String value) {
		queryParams.put(key, value);
		return this;
	}

	public RestClientBuilder setRequestBody(Object body) {
		this.requestBody = body;
		return this;
	}

	public Response execute() {
		try {
			RequestSpecification request = RestAssured.given();

			LoggerUtil.logInfo("Making request to: " + baseUrl + endpoint);
			LoggerUtil.logInfo("Headers: " + headers);

			// Add headers
			headers.forEach(request::header);

			// Add query parameters
			queryParams.forEach(request::queryParam);

			// Add request body if present
			if (requestBody != null) {
				request.body(requestBody);
			}

			// Log request details
			LoggerUtil.logInfo(String.format("Executing %s request to %s%s", method, baseUrl, endpoint));

			// Execute request based on method
			Response response = switch (method.toUpperCase()) {
			case "GET" -> request.get(baseUrl + endpoint);
			case "POST" -> request.post(baseUrl + endpoint);
			case "PUT" -> request.put(baseUrl + endpoint);
			case "DELETE" -> request.delete(baseUrl + endpoint);
			case "PATCH" -> request.patch(baseUrl + endpoint);
			default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
			};

			// Log response status
			LoggerUtil.logInfo("Response Status: " + response.getStatusCode());
			return response;

		} catch (Exception e) {
			LoggerUtil.logError("API request failed", e);
			throw new RuntimeException("API request failed", e);
		}
	}
}