package com.umr.apitesting.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.umr.apitesting.core.config.ConfigManager;
import com.umr.apitesting.core.http.RestClientBuilder;
import com.umr.apitesting.utils.LoggerUtil;

import io.restassured.response.Response;
import lombok.Data;

public class AuthManager {
	private static final Map<String, AuthConfig> authConfigs = new ConcurrentHashMap<>();
	private static final Map<String, String> tokenCache = new ConcurrentHashMap<>();
	private static final Map<String, Long> tokenExpiry = new ConcurrentHashMap<>();

	@Data
	public static class AuthConfig {
		private String authUrl;
		private String tokenPath; // JsonPath to extract token
		private String clientId;
		private String clientSecret;
		private int expirySeconds;
		private Map<String, String> additionalParams;
	}

	public static void loadAuthConfig(String configPath) {
		Properties props = new Properties();
		try (FileInputStream fis = new FileInputStream(configPath)) {
			props.load(fis);
			AuthConfig config = new AuthConfig();
			config.setAuthUrl(props.getProperty("auth.url"));
			config.setTokenPath(props.getProperty("auth.token.path", "$.access_token"));
			config.setClientId(props.getProperty("auth.client.id"));
			config.setClientSecret(props.getProperty("auth.client.secret"));
			config.setExpirySeconds(Integer.parseInt(props.getProperty("auth.token.expiry", "3600")));
			authConfigs.put(ConfigManager.getCurrentEnvironment(), config);
		} catch (IOException e) {
			LoggerUtil.logError("Failed to load auth config", e);
		}
	}

	public static String getToken() {
		String env = ConfigManager.getCurrentEnvironment();
		if (isTokenValid(env)) {
			return tokenCache.get(env);
		}
		return generateNewToken(env);
	}

	private static String generateNewToken(String env) {
		AuthConfig config = authConfigs.get(env);
		RestClientBuilder builder = new RestClientBuilder().setBaseUrl(config.getAuthUrl()).setEndpoint("/token")
				.setMethod("POST").setRequestBody(Map.of("grant_type", "client_credentials", "client_id",
						config.getClientId(), "client_secret", config.getClientSecret()));

		Response response = builder.execute();
		String token = response.jsonPath().getString(config.getTokenPath());
		tokenCache.put(env, token);
		tokenExpiry.put(env, System.currentTimeMillis() + (config.getExpirySeconds() * 1000L));
		return token;
	}

	private static boolean isTokenValid(String env) {
		String token = tokenCache.get(env);
		Long expiry = tokenExpiry.get(env);
		return token != null && expiry != null && System.currentTimeMillis() < expiry;
	}
}