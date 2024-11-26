package com.umr.apitesting.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;

public class AuthManager {
	private static final Map<String, String> tokenCache = new ConcurrentHashMap<>();
	private static final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();
	private static final int DEFAULT_TOKEN_VALIDITY = 3600; // 1 hour in seconds

	public static String getToken(String type) {
		String cachedToken = tokenCache.get(type);
		if (isTokenValid(type, cachedToken)) {
			return cachedToken;
		}
		return generateNewToken(type);
	}

	private static boolean isTokenValid(String type, String token) {
		if (token == null)
			return false;
		Long expiryTime = tokenExpiry.get(type);
		if (expiryTime == null)
			return false;
		return System.currentTimeMillis() < expiryTime;
	}

	private static String generateNewToken(String type) {
		try {
			// Example token generation using REST call
			Response response = new RestClientBuilder().setBaseUrl(System.getProperty("auth.url"))
					.setEndpoint("/auth/token").setMethod("POST").addHeader("Content-Type", "application/json")
					.setRequestBody(Map.of("clientId", System.getProperty("auth.clientId"), "clientSecret",
							System.getProperty("auth.clientSecret")))
					.execute();

			if (response.getStatusCode() == 200) {
				String token = response.jsonPath().getString("access_token");
				int expiresIn = response.jsonPath().getInt("expires_in", DEFAULT_TOKEN_VALIDITY);

				tokenCache.put(type, token);
				tokenExpiry.put(type, System.currentTimeMillis() + (expiresIn * 1000));

				return token;
			}
			throw new RuntimeException("Failed to generate token: " + response.getStatusCode());
		} catch (Exception e) {
			LoggerUtil.logError("Token generation failed", e);
			throw new RuntimeException("Failed to generate token", e);
		}
	}

	public static void clearTokens() {
		tokenCache.clear();
		tokenExpiry.clear();
	}
}