package com.umr.apitesting.core.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.umr.apitesting.utils.LoggerUtil;

public class ConfigManager {
	private static final String CONFIG_PATH = "src/main/resources/config/";
	private static final ConcurrentHashMap<String, Properties> envProperties = new ConcurrentHashMap<>();
	private static String currentEnvironment;

	public static void loadEnvironmentConfig(String environment) {
		String configFile = CONFIG_PATH + environment + ".properties";
		Properties props = new Properties();

		try (FileInputStream fis = new FileInputStream(configFile)) {
			props.load(fis);
			envProperties.put(environment, props);
			currentEnvironment = environment;
			LoggerUtil.logInfo("Loaded configuration for environment: " + environment);
		} catch (IOException e) {
			LoggerUtil.logError("Failed to load configuration for environment: " + environment, e);
			throw new RuntimeException("Failed to load configuration", e);
		}
	}

	public static String getProperty(String key) {
		if (currentEnvironment == null) {
			throw new RuntimeException("Environment not set. Call loadEnvironmentConfig first.");
		}

		Properties props = envProperties.get(currentEnvironment);
		if (props == null) {
			throw new RuntimeException("No properties loaded for environment: " + currentEnvironment);
		}

		return props.getProperty(key);
	}

	public static String getCurrentEnvironment() {
		return currentEnvironment;
	}
}
